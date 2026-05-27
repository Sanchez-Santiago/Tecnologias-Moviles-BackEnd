package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

object FinancialTransactionsTable : UUIDTable("financial_transactions") {
    val groupId: Column<EntityID<UUID>> = reference("group_id", GroupsTable)
    val userId: Column<EntityID<UUID>> = reference("user_id", UsersTable)
    val type: Column<String> = varchar("type", 20)
    val category: Column<String> = varchar("category", 100)
    val amount: Column<BigDecimal> = decimal("amount", 12, 2)
    val description: Column<String?> = text("description").nullable()
    val transactionDate: Column<LocalDateTime> = datetime("transaction_date").clientDefault { LocalDateTime.now() }
    val active: Column<Boolean> = bool("active").default(true)
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt: Column<LocalDateTime> = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
