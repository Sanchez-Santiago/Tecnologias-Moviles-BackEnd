package com.misuper.backend.modules.transactions.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.FinancialTransactionsTable
import com.misuper.backend.database.tables.GroupsTable
import com.misuper.backend.database.tables.UsersTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class FinancialTransactionRepository {
    private val db get() = DatabaseFactory.getDatabase()

    fun findByGroup(groupId: UUID): List<ResultRow> = transaction(db) {
        FinancialTransactionsTable.selectAll()
            .where {
                (FinancialTransactionsTable.groupId eq EntityID(groupId, GroupsTable)) and
                    (FinancialTransactionsTable.active eq true)
            }
            .orderBy(FinancialTransactionsTable.transactionDate, SortOrder.DESC)
            .toList()
    }

    fun findById(id: UUID): ResultRow? = transaction(db) {
        FinancialTransactionsTable.selectAll()
            .where { (FinancialTransactionsTable.id eq id) and (FinancialTransactionsTable.active eq true) }
            .singleOrNull()
    }

    fun create(
        groupId: UUID,
        userId: UUID,
        type: String,
        category: String,
        amount: BigDecimal,
        description: String?,
        transactionDate: LocalDateTime
    ): UUID = transaction(db) {
        FinancialTransactionsTable.insert {
            it[FinancialTransactionsTable.groupId] = EntityID(groupId, GroupsTable)
            it[FinancialTransactionsTable.userId] = EntityID(userId, UsersTable)
            it[FinancialTransactionsTable.type] = type
            it[FinancialTransactionsTable.category] = category
            it[FinancialTransactionsTable.amount] = amount
            it[FinancialTransactionsTable.description] = description
            it[FinancialTransactionsTable.transactionDate] = transactionDate
        }[FinancialTransactionsTable.id].value
    }

    fun softDelete(id: UUID) = transaction(db) {
        FinancialTransactionsTable.update({ FinancialTransactionsTable.id eq id }) {
            it[active] = false
            it[updatedAt] = LocalDateTime.now()
        }
    }
}
