package com.misuper.backend.modules.budgets.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.BudgetItemsTable
import com.misuper.backend.database.tables.BudgetsTable
import com.misuper.backend.database.tables.CategoriesTable
import com.misuper.backend.database.tables.GroupsTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class BudgetRepository {

    private val db get() = DatabaseFactory.getDatabase()

    fun findById(id: UUID): ResultRow? = transaction(db) {
        BudgetsTable.selectAll().where { BudgetsTable.id eq id }.singleOrNull()
    }

    fun findByGroupId(groupIdVal: UUID): List<ResultRow> = transaction(db) {
        BudgetsTable.selectAll()
            .where {
                (BudgetsTable.groupId eq EntityID(groupIdVal, GroupsTable)) and
                    (BudgetsTable.active eq true)
            }
            .orderBy(BudgetsTable.createdAt, SortOrder.DESC_NULLS_LAST)
            .toList()
    }

    fun create(
        groupIdVal: UUID,
        nameVal: String,
        totalAmountVal: BigDecimal,
        periodVal: String,
        startDateVal: LocalDateTime,
        endDateVal: LocalDateTime?
    ): UUID = transaction(db) {
        BudgetsTable.insert { stmt ->
            stmt[BudgetsTable.groupId] = EntityID(groupIdVal, GroupsTable)
            stmt[BudgetsTable.name] = nameVal
            stmt[BudgetsTable.totalAmount] = totalAmountVal
            stmt[BudgetsTable.period] = periodVal
            stmt[BudgetsTable.startDate] = startDateVal
            stmt[BudgetsTable.endDate] = endDateVal
        }[BudgetsTable.id].value
    }

    fun createWithItems(
        groupIdVal: UUID,
        nameVal: String,
        totalAmountVal: BigDecimal,
        periodVal: String,
        startDateVal: LocalDateTime,
        endDateVal: LocalDateTime?,
        items: List<BudgetItemInsert>
    ): UUID = transaction(db) {
        val budgetId = BudgetsTable.insert { stmt ->
            stmt[BudgetsTable.groupId] = EntityID(groupIdVal, GroupsTable)
            stmt[BudgetsTable.name] = nameVal
            stmt[BudgetsTable.totalAmount] = totalAmountVal
            stmt[BudgetsTable.period] = periodVal
            stmt[BudgetsTable.startDate] = startDateVal
            stmt[BudgetsTable.endDate] = endDateVal
        }[BudgetsTable.id].value

        items.forEach { item ->
            BudgetItemsTable.insert { stmt ->
                stmt[BudgetItemsTable.budgetId] = EntityID(budgetId, BudgetsTable)
                stmt[BudgetItemsTable.categoryId] = EntityID(item.categoryId, CategoriesTable)
                stmt[BudgetItemsTable.amount] = item.amount
            }
        }

        budgetId
    }

    fun update(
        id: UUID,
        nameVal: String?,
        totalAmountVal: BigDecimal?,
        periodVal: String?,
        startDateVal: LocalDateTime?,
        endDateVal: LocalDateTime?
    ) = transaction(db) {
        BudgetsTable.update({ BudgetsTable.id eq id }) { stmt ->
            nameVal?.let { stmt[BudgetsTable.name] = it }
            totalAmountVal?.let { stmt[BudgetsTable.totalAmount] = it }
            periodVal?.let { stmt[BudgetsTable.period] = it }
            startDateVal?.let { stmt[BudgetsTable.startDate] = it }
            if (endDateVal != null) stmt[BudgetsTable.endDate] = endDateVal
            stmt[BudgetsTable.updatedAt] = LocalDateTime.now()
        }
    }

    fun updateWithItems(
        id: UUID,
        nameVal: String?,
        totalAmountVal: BigDecimal?,
        periodVal: String?,
        startDateVal: LocalDateTime?,
        endDateVal: LocalDateTime?,
        items: List<BudgetItemInsert>?
    ) = transaction(db) {
        BudgetsTable.update({ BudgetsTable.id eq id }) { stmt ->
            nameVal?.let { stmt[BudgetsTable.name] = it }
            totalAmountVal?.let { stmt[BudgetsTable.totalAmount] = it }
            periodVal?.let { stmt[BudgetsTable.period] = it }
            startDateVal?.let { stmt[BudgetsTable.startDate] = it }
            if (endDateVal != null) stmt[BudgetsTable.endDate] = endDateVal
            stmt[BudgetsTable.updatedAt] = LocalDateTime.now()
        }

        if (items != null) {
            BudgetItemsTable.deleteWhere {
                budgetId eq EntityID(id, BudgetsTable)
            }
            items.forEach { item ->
                BudgetItemsTable.insert { stmt ->
                    stmt[BudgetItemsTable.budgetId] = EntityID(id, BudgetsTable)
                    stmt[BudgetItemsTable.categoryId] = EntityID(item.categoryId, CategoriesTable)
                    stmt[BudgetItemsTable.amount] = item.amount
                }
            }
        }
    }

    fun activate(id: UUID, groupIdVal: UUID) = transaction(db) {
        BudgetsTable.update({ BudgetsTable.groupId eq EntityID(groupIdVal, GroupsTable) }) { stmt ->
            stmt[BudgetsTable.active] = false
            stmt[BudgetsTable.updatedAt] = LocalDateTime.now()
        }
        BudgetsTable.update({ BudgetsTable.id eq id }) { stmt ->
            stmt[BudgetsTable.active] = true
            stmt[BudgetsTable.updatedAt] = LocalDateTime.now()
        }
    }

    fun softDelete(id: UUID) = transaction(db) {
        BudgetsTable.update({ BudgetsTable.id eq id }) { stmt ->
            stmt[BudgetsTable.active] = false
            stmt[BudgetsTable.updatedAt] = LocalDateTime.now()
        }
    }

    fun getItems(budgetIdVal: UUID): List<ResultRow> = transaction(db) {
        BudgetItemsTable.selectAll()
            .where {
                (BudgetItemsTable.budgetId eq EntityID(budgetIdVal, BudgetsTable)) and
                    (BudgetItemsTable.active eq true)
            }
            .toList()
    }

    fun addItem(
        budgetIdVal: UUID,
        categoryIdVal: UUID,
        amountVal: BigDecimal
    ) = transaction(db) {
        BudgetItemsTable.insert { stmt ->
            stmt[BudgetItemsTable.budgetId] = EntityID(budgetIdVal, BudgetsTable)
            stmt[BudgetItemsTable.categoryId] = EntityID(categoryIdVal, CategoriesTable)
            stmt[BudgetItemsTable.amount] = amountVal
        }
    }

    fun deleteItems(budgetIdVal: UUID) = transaction(db) {
        BudgetItemsTable.deleteWhere {
            budgetId eq EntityID(budgetIdVal, BudgetsTable)
        }
    }

    fun findCategoryById(id: UUID): ResultRow? = transaction(db) {
        CategoriesTable.selectAll().where { CategoriesTable.id eq id }.singleOrNull()
    }
}

data class BudgetItemInsert(
    val categoryId: UUID,
    val amount: BigDecimal
)
