package com.misuper.backend.modules.statistics.services

import com.misuper.backend.database.tables.BudgetItemsTable
import com.misuper.backend.database.tables.BudgetsTable
import com.misuper.backend.database.tables.CategoriesTable
import com.misuper.backend.database.tables.ProductsTable
import com.misuper.backend.database.tables.PurchaseProductsTable
import com.misuper.backend.database.tables.PurchasesTable
import com.misuper.backend.database.tables.StoresTable
import com.misuper.backend.exceptions.ForbiddenException
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.modules.budgets.repositories.BudgetRepository
import com.misuper.backend.modules.groups.repositories.GroupRepository
import com.misuper.backend.modules.products.repositories.CategoryRepository
import com.misuper.backend.modules.products.repositories.ProductRepository
import com.misuper.backend.modules.purchases.repositories.PurchaseRepository
import com.misuper.backend.modules.statistics.dto.BudgetProgress
import com.misuper.backend.modules.statistics.dto.MonthlySummary
import com.misuper.backend.modules.statistics.dto.SpendingByCategory
import com.misuper.backend.modules.statistics.dto.SpendingByStore
import com.misuper.backend.modules.stores.repositories.StoreRepository
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.UUID

class StatisticsService(
    private val purchaseRepository: PurchaseRepository,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val storeRepository: StoreRepository,
    private val budgetRepository: BudgetRepository,
    private val groupRepository: GroupRepository
) {
    fun getSpendingByCategory(groupId: UUID, userId: UUID): List<SpendingByCategory> {
        checkMembership(groupId, userId)

        val purchases = purchaseRepository.findByGroupId(groupId)
        val grandTotal = purchases.sumOf { it[PurchasesTable.total] }

        val categoryTotals = mutableMapOf<UUID, BigDecimal>()

        val processed = mutableSetOf<UUID>()
        purchases.forEach { purchase ->
            val purchaseId = purchase[PurchasesTable.id].value
            if (purchaseId in processed) return@forEach
            processed.add(purchaseId)

            val items = purchaseRepository.getItems(purchaseId)
            items.forEach { item ->
                val productId = item[PurchaseProductsTable.productId].value
                val productRow = productRepository.findById(productId)
                val categoryId = productRow?.let { row ->
                    try { row[ProductsTable.categoryId].value } catch (_: Exception) { null }
                }
                val catId = categoryId
                if (catId != null) {
                    categoryTotals.merge(catId, item[PurchaseProductsTable.subtotal], BigDecimal::add)
                }
            }
        }

        val total = if (grandTotal > BigDecimal.ZERO) grandTotal else BigDecimal.ONE

        return categoryTotals.map { (catId, totalSpent) ->
            val catRow = categoryRepository.findById(catId)
            SpendingByCategory(
                categoryId = catId.toString(),
                categoryName = catRow?.get(CategoriesTable.name) ?: "Desconocida",
                total = totalSpent,
                percentage = totalSpent.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .toDouble()
            )
        }.sortedByDescending { it.total }
    }

    fun getSpendingByStore(groupId: UUID, userId: UUID): List<SpendingByStore> {
        checkMembership(groupId, userId)

        val purchases = purchaseRepository.findByGroupId(groupId)
        val grandTotal = purchases.sumOf { it[PurchasesTable.total] }

        val storeTotals = mutableMapOf<UUID?, BigDecimal>()
        purchases.forEach { purchase ->
            val storeId = purchase[PurchasesTable.storeId]
            val total = purchase[PurchasesTable.total]
            storeTotals.merge(storeId?.value, total, BigDecimal::add)
        }

        val total = if (grandTotal > BigDecimal.ZERO) grandTotal else BigDecimal.ONE

        return storeTotals.map { (storeId, totalSpent) ->
            val storeName = storeId?.let { sid ->
                storeRepository.findById(sid)?.get(StoresTable.name)
            } ?: "Sin tienda"
            SpendingByStore(
                storeId = storeId?.toString(),
                storeName = storeName,
                total = totalSpent,
                percentage = totalSpent.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .toDouble()
            )
        }.sortedByDescending { it.total }
    }

    fun getMonthlySummary(groupId: UUID, userId: UUID): List<MonthlySummary> {
        checkMembership(groupId, userId)

        val purchases = purchaseRepository.findByGroupId(groupId)
        val monthly = mutableMapOf<Pair<Int, Int>, MutableList<BigDecimal>>()

        purchases.forEach { purchase ->
            val date = purchase[PurchasesTable.createdAt]
            val year = date.year
            val month = date.monthValue
            val total = purchase[PurchasesTable.total]
            monthly.getOrPut(year to month) { mutableListOf() }.add(total)
        }

        return monthly.map { (yearMonth, totals) ->
            MonthlySummary(
                year = yearMonth.first,
                month = yearMonth.second,
                total = totals.sumOf { it },
                purchaseCount = totals.size
            )
        }.sortedBy { (it.year * 100) + it.month }
    }

    fun getBudgetProgress(groupId: UUID, userId: UUID): List<BudgetProgress> {
        checkMembership(groupId, userId)

        val purchases = purchaseRepository.findByGroupId(groupId)
        val totalSpent = purchases.sumOf { it[PurchasesTable.total] }

        val budgets = budgetRepository.findByGroupId(groupId)

        return budgets.map { budgetRow ->
            val budgetId = budgetRow[BudgetsTable.id].value
            val budgetAmount = budgetRow[BudgetsTable.totalAmount]
            val spent = if (budgetAmount > BigDecimal.ZERO) {
                val items = budgetRepository.getItems(budgetId)
                val now = LocalDate.now()
                val start = budgetRow[BudgetsTable.startDate].toLocalDate()
                val end = budgetRow[BudgetsTable.endDate]?.toLocalDate() ?: start.plusMonths(1)
                if (now in start..end) totalSpent else BigDecimal.ZERO
            } else BigDecimal.ZERO

            val percentageUsed = if (budgetAmount > BigDecimal.ZERO) {
                spent.divide(budgetAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .toDouble()
            } else 0.0

            BudgetProgress(
                budgetId = budgetId.toString(),
                budgetName = budgetRow[BudgetsTable.name],
                budgetAmount = budgetAmount,
                spent = spent,
                percentageUsed = percentageUsed,
                period = budgetRow[BudgetsTable.period]
            )
        }
    }

    private fun checkMembership(groupId: UUID, userId: UUID) {
        val group = groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")
        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")
    }
}
