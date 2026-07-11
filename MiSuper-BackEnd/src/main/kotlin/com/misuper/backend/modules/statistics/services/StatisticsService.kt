package com.misuper.backend.modules.statistics.services

import com.misuper.backend.database.tables.BudgetsTable
import com.misuper.backend.database.tables.CategoriesTable
import com.misuper.backend.database.tables.ProductsTable
import com.misuper.backend.database.tables.TicketProductsTable
import com.misuper.backend.database.tables.TicketsTable
import com.misuper.backend.database.tables.UsersTable
import com.misuper.backend.exceptions.ForbiddenException
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.modules.budgets.repositories.BudgetRepository
import com.misuper.backend.modules.groups.repositories.GroupRepository
import com.misuper.backend.modules.products.repositories.CategoryRepository
import com.misuper.backend.modules.products.repositories.ProductRepository
import com.misuper.backend.modules.statistics.dto.BudgetProgress
import com.misuper.backend.modules.statistics.dto.MemberSpending
import com.misuper.backend.modules.statistics.dto.MonthlySummary
import com.misuper.backend.modules.statistics.dto.MostPurchasedProduct
import com.misuper.backend.modules.statistics.dto.SpendingByCategory
import com.misuper.backend.modules.statistics.dto.SpendingByImportance
import com.misuper.backend.modules.statistics.dto.SpendingByStore
import com.misuper.backend.modules.statistics.dto.StoreFrequency
import com.misuper.backend.modules.tickets.repositories.TicketRepository
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

class StatisticsService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val groupRepository: GroupRepository,
    private val ticketRepository: TicketRepository
) {
    fun getSpendingByCategory(groupId: UUID, userId: UUID, from: LocalDate? = null, to: LocalDate? = null): List<SpendingByCategory> {
        checkMembership(groupId, userId)

        val (fromDate, toDate) = resolveDateRange(from, to)
        val tickets = ticketRepository.findExpensesByGroupIdAndDateRange(groupId, fromDate, toDate)
        if (tickets.isEmpty()) return emptyList()

        val categoryTotals = mutableMapOf<UUID, BigDecimal>()
        var grandTotal = BigDecimal.ZERO

        tickets.forEach { ticket ->
            val ticketId = ticket[TicketsTable.id].value
            val items = ticketRepository.getProducts(ticketId)
            items.forEach { item ->
                val productId = item[TicketProductsTable.productId]?.value ?: return@forEach
                val productRow = productRepository.findById(productId)
                val categoryId = productRow?.let { row ->
                    try { row[ProductsTable.categoryId].value } catch (_: Exception) { null }
                }
                val subtotal = item[TicketProductsTable.price]?.let { price ->
                    item[TicketProductsTable.quantity]?.let { qty -> price.multiply(qty) }
                } ?: BigDecimal.ZERO

                if (categoryId != null) {
                    categoryTotals.merge(categoryId, subtotal, BigDecimal::add)
                }
                grandTotal = grandTotal.add(subtotal)
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

    fun getSpendingByStore(groupId: UUID, userId: UUID, from: LocalDate? = null, to: LocalDate? = null): List<SpendingByStore> {
        checkMembership(groupId, userId)

        val (fromDate, toDate) = resolveDateRange(from, to)
        val tickets = ticketRepository.findExpensesByGroupIdAndDateRange(groupId, fromDate, toDate)
        if (tickets.isEmpty()) return emptyList()

        val storeTotals = mutableMapOf<String, BigDecimal>()
        var grandTotal = BigDecimal.ZERO

        tickets.forEach { ticket ->
            val storeName = ticket[TicketsTable.supermarketName]
            val amount = ticket[TicketsTable.amount]
            storeTotals.merge(storeName, amount, BigDecimal::add)
            grandTotal = grandTotal.add(amount)
        }

        val total = if (grandTotal > BigDecimal.ZERO) grandTotal else BigDecimal.ONE

        return storeTotals.map { (storeName, totalSpent) ->
            SpendingByStore(
                storeId = null,
                storeName = storeName,
                total = totalSpent,
                percentage = totalSpent.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .toDouble()
            )
        }.sortedByDescending { it.total }
    }

    fun getMonthlySummary(groupId: UUID, userId: UUID, from: LocalDate? = null, to: LocalDate? = null): List<MonthlySummary> {
        checkMembership(groupId, userId)

        val (fromDate, toDate) = resolveDateRange(from, to)
        val tickets = ticketRepository.findExpensesByGroupIdAndDateRange(groupId, fromDate, toDate)
        val monthly = mutableMapOf<Pair<Int, Int>, MutableList<BigDecimal>>()

        tickets.forEach { ticket ->
            val date = ticket[TicketsTable.purchaseDate]
            val year = date.year
            val month = date.monthValue
            val amount = ticket[TicketsTable.amount]
            monthly.getOrPut(year to month) { mutableListOf() }.add(amount)
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

    fun getBudgetProgress(groupId: UUID, userId: UUID, from: LocalDate? = null, to: LocalDate? = null): List<BudgetProgress> {
        checkMembership(groupId, userId)

        val (fromDate, toDate) = resolveDateRange(from, to)
        val budgets = budgetRepository.findByDateRange(groupId, fromDate, toDate)
        if (budgets.isEmpty()) return emptyList()

        return budgets.map { budgetRow ->
            val budgetStart = budgetRow[BudgetsTable.startDate]
            val budgetEnd = budgetRow[BudgetsTable.endDate]
            val budgetAmount = budgetRow[BudgetsTable.total]

            val tickets = ticketRepository.findExpensesByGroupIdAndDateRange(
                groupId, budgetStart, budgetEnd
            )
            val spent = tickets.sumOf { it[TicketsTable.amount] }

            val percentageUsed = if (budgetAmount > BigDecimal.ZERO) {
                spent.divide(budgetAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .toDouble()
            } else 0.0

            BudgetProgress(
                budgetId = budgetRow[BudgetsTable.id].value.toString(),
                budgetName = "Presupuesto ${budgetStart} - ${budgetEnd}",
                budgetAmount = budgetAmount,
                spent = spent,
                percentageUsed = percentageUsed,
                period = "${budgetStart} / ${budgetEnd}"
            )
        }
    }

    fun getSpendingByImportance(groupId: UUID, userId: UUID, from: LocalDate? = null, to: LocalDate? = null): List<SpendingByImportance> {
        checkMembership(groupId, userId)

        val (fromDate, toDate) = resolveDateRange(from, to)
        val tickets = ticketRepository.findExpensesByGroupIdAndDateRange(groupId, fromDate, toDate)
        if (tickets.isEmpty()) return emptyList()

        val importanceTotals = mutableMapOf<String, MutableList<BigDecimal>>()
        val ticketIdsByImportance = mutableMapOf<String, MutableSet<UUID>>()
        val itemCountByImportance = mutableMapOf<String, Int>()
        var grandTotal = BigDecimal.ZERO

        tickets.forEach { ticket ->
            val ticketId = ticket[TicketsTable.id].value
            val items = ticketRepository.getProducts(ticketId)
            items.forEach { item ->
                val productId = item[TicketProductsTable.productId]?.value ?: return@forEach
                val productRow = productRepository.findById(productId)
                val priority = productRow?.let { row ->
                    try { row[ProductsTable.priority] } catch (_: Exception) { "SECUNDARIO" }
                } ?: "SECUNDARIO"

                val normalized = when (priority.uppercase()) {
                    "ESENCIAL" -> "ESENCIAL"
                    "PRIMARIO" -> "PRIMARIO"
                    else -> "SECUNDARIO"
                }

                val subtotal = item[TicketProductsTable.price]?.let { price ->
                    item[TicketProductsTable.quantity]?.let { qty -> price.multiply(qty) }
                } ?: BigDecimal.ZERO

                importanceTotals.getOrPut(normalized) { mutableListOf() }.add(subtotal)
                ticketIdsByImportance.getOrPut(normalized) { mutableSetOf() }.add(ticketId)
                itemCountByImportance.merge(normalized, 1, Int::plus)
                grandTotal = grandTotal.add(subtotal)
            }
        }

        val total = if (grandTotal > BigDecimal.ZERO) grandTotal else BigDecimal.ONE
        val order = listOf("ESENCIAL", "PRIMARIO", "SECUNDARIO")

        return order.mapNotNull { imp ->
            val totals = importanceTotals[imp] ?: return@mapNotNull null
            val sum = totals.sumOf { it }
            SpendingByImportance(
                importance = imp,
                total = sum,
                percentage = sum.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .toDouble(),
                purchaseCount = ticketIdsByImportance[imp]?.size ?: 0,
                itemCount = itemCountByImportance[imp] ?: 0
            )
        }
    }

    fun getMostFrequentStore(groupId: UUID, userId: UUID, from: LocalDate? = null, to: LocalDate? = null): List<StoreFrequency> {
        checkMembership(groupId, userId)

        val (fromDate, toDate) = resolveDateRange(from, to)
        val tickets = ticketRepository.findExpensesByGroupIdAndDateRange(groupId, fromDate, toDate)
        if (tickets.isEmpty()) return emptyList()

        val grandTotal = tickets.sumOf { it[TicketsTable.amount] }
        val totalCount = tickets.size

        val storeStats = mutableMapOf<String, MutableList<BigDecimal>>()
        tickets.forEach { ticket ->
            val storeName = ticket[TicketsTable.supermarketName]
            val amount = ticket[TicketsTable.amount]
            storeStats.getOrPut(storeName) { mutableListOf() }.add(amount)
        }

        val countTotal = if (totalCount > 0) totalCount.toBigDecimal() else BigDecimal.ONE

        return storeStats.map { (storeName, totals) ->
            StoreFrequency(
                storeId = null,
                storeName = storeName,
                purchaseCount = totals.size,
                totalSpent = totals.sumOf { it },
                percentage = totals.size.toBigDecimal()
                    .divide(countTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .toDouble()
            )
        }.sortedByDescending { it.purchaseCount }
    }

    fun getMostPurchasedProducts(groupId: UUID, userId: UUID, from: LocalDate? = null, to: LocalDate? = null): List<MostPurchasedProduct> {
        checkMembership(groupId, userId)

        val (fromDate, toDate) = resolveDateRange(from, to)
        val tickets = ticketRepository.findExpensesByGroupIdAndDateRange(groupId, fromDate, toDate)
        if (tickets.isEmpty()) return emptyList()

        val productCounts = mutableMapOf<String, Int>()
        val productTotals = mutableMapOf<String, BigDecimal>()

        tickets.forEach { ticket ->
            val ticketId = ticket[TicketsTable.id].value
            val items = ticketRepository.getProducts(ticketId)
            items.forEach { item ->
                val productId = item[TicketProductsTable.productId]?.value ?: return@forEach
                val productRow = productRepository.findById(productId)
                val productName = productRow?.get(ProductsTable.name) ?: "Producto Desconocido"

                val quantity = item[TicketProductsTable.quantity]?.toInt() ?: 0
                val subtotal = item[TicketProductsTable.price]?.let { price ->
                    item[TicketProductsTable.quantity]?.let { qty -> price.multiply(qty) }
                } ?: BigDecimal.ZERO

                productCounts.merge(productName, quantity, Int::plus)
                productTotals.merge(productName, subtotal, BigDecimal::add)
            }
        }

        return productCounts.map { (name, count) ->
            MostPurchasedProduct(
                productName = name,
                count = count,
                totalSpent = productTotals[name] ?: BigDecimal.ZERO
            )
        }.sortedByDescending { it.count }.take(10)
    }

    fun getMemberSpending(groupId: UUID, userId: UUID, from: LocalDate? = null, to: LocalDate? = null): List<MemberSpending> {
        checkMembership(groupId, userId)

        val (fromDate, toDate) = resolveDateRange(from, to)
        val tickets = ticketRepository.findExpensesByGroupIdAndDateRange(groupId, fromDate, toDate)
        if (tickets.isEmpty()) return emptyList()

        val grandTotal = tickets.sumOf { it[TicketsTable.amount] }
        val memberTotals = mutableMapOf<UUID, BigDecimal>()
        val memberCounts = mutableMapOf<UUID, Int>()

        tickets.forEach { ticket ->
            val uId = ticket[TicketsTable.uploadedBy].value
            val amount = ticket[TicketsTable.amount]
            memberTotals.merge(uId, amount, BigDecimal::add)
            memberCounts.merge(uId, 1, Int::plus)
        }

        val total = if (grandTotal > BigDecimal.ZERO) grandTotal else BigDecimal.ONE
        val userNameCache = mutableMapOf<UUID, String>()

        return memberTotals.map { (uId, totalSpent) ->
            val userName = userNameCache.getOrPut(uId) {
                groupRepository.findUserById(uId)?.get(UsersTable.fullName) ?: "Usuario"
            }
            MemberSpending(
                userId = uId.toString(),
                userName = userName,
                totalSpent = totalSpent,
                percentage = totalSpent.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .toDouble(),
                purchaseCount = memberCounts[uId] ?: 0
            )
        }.sortedByDescending { it.totalSpent }
    }

    private fun resolveDateRange(from: LocalDate?, to: LocalDate?): Pair<LocalDate, LocalDate> {
        val fromDate = from ?: LocalDate.now().withDayOfMonth(1)
        val toDate = to ?: LocalDate.now()
        return fromDate to toDate
    }

    private fun checkMembership(groupId: UUID, userId: UUID) {
        groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")
    }
}
