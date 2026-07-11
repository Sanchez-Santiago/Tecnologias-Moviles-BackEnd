package com.misuper.backend.modules.budgets.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.BudgetsTable
import com.misuper.backend.database.tables.GroupsTable
import com.misuper.backend.database.tables.UsersTable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class BudgetRepository {

    private val db get() = DatabaseFactory.getDatabase()

    fun findById(id: UUID): ResultRow? = transaction(db) {
        BudgetsTable.selectAll().where { BudgetsTable.id eq id }.singleOrNull()
    }

    fun findByGroupId(groupId: UUID): List<ResultRow> = transaction(db) {
        BudgetsTable.selectAll()
            .where { BudgetsTable.groupId eq EntityID(groupId, GroupsTable) }
            .orderBy(BudgetsTable.createdAt, SortOrder.DESC)
            .toList()
    }

    fun findCurrentByGroupId(groupId: UUID): ResultRow? = transaction(db) {
        val today = LocalDate.now()
        BudgetsTable.selectAll()
            .where {
                (BudgetsTable.groupId eq EntityID(groupId, GroupsTable)) and
                    (BudgetsTable.startDate lessEq today) and
                    (BudgetsTable.endDate greaterEq today)
            }
            .orderBy(BudgetsTable.createdAt, SortOrder.DESC)
            .firstOrNull()
    }

    fun findByDateRange(groupId: UUID, from: LocalDate, to: LocalDate): List<ResultRow> = transaction(db) {
        BudgetsTable.selectAll()
            .where {
                (BudgetsTable.groupId eq EntityID(groupId, GroupsTable)) and
                    (BudgetsTable.startDate lessEq to) and
                    (BudgetsTable.endDate greaterEq from)
            }
            .orderBy(BudgetsTable.createdAt, SortOrder.DESC)
            .toList()
    }

    fun create(groupId: UUID, startDate: LocalDate, endDate: LocalDate, total: BigDecimal, createdBy: UUID): UUID = transaction(db) {
        BudgetsTable.insert { stmt ->
            stmt[BudgetsTable.groupId] = EntityID(groupId, GroupsTable)
            stmt[BudgetsTable.startDate] = startDate
            stmt[BudgetsTable.endDate] = endDate
            stmt[BudgetsTable.total] = total
            stmt[BudgetsTable.createdBy] = EntityID(createdBy, UsersTable)
        }[BudgetsTable.id].value
    }

    fun update(id: UUID, total: BigDecimal?, startDate: LocalDate?, endDate: LocalDate?) = transaction(db) {
        BudgetsTable.update({ BudgetsTable.id eq id }) { stmt ->
            total?.let { stmt[BudgetsTable.total] = it }
            startDate?.let { stmt[BudgetsTable.startDate] = it }
            endDate?.let { stmt[BudgetsTable.endDate] = it }
            stmt[BudgetsTable.updatedAt] = LocalDateTime.now()
        }
    }

    fun delete(id: UUID) = transaction(db) {
        BudgetsTable.deleteWhere { BudgetsTable.id eq id }
    }
}
