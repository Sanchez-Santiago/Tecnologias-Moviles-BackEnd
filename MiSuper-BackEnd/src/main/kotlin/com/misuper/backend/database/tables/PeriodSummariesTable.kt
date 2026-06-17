package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

object PeriodSummariesTable : UUIDTable("period_summaries") {
    val periodId: Column<EntityID<UUID>> = reference("period_id", GroupMonthlyPeriodsTable).uniqueIndex()
    val totalIncome: Column<BigDecimal> = decimal("total_income", 12, 2).default(BigDecimal.ZERO)
    val totalExpense: Column<BigDecimal> = decimal("total_expense", 12, 2).default(BigDecimal.ZERO)
    val totalPurchases: Column<BigDecimal> = decimal("total_purchases", 12, 2).default(BigDecimal.ZERO)
    val purchaseCount: Column<Int> = integer("purchase_count").default(0)
    val productCount: Column<Int> = integer("product_count").default(0)
    val mostExpensivePurchase: Column<BigDecimal?> = decimal("most_expensive_purchase", 12, 2).nullable()
    val averagePurchase: Column<BigDecimal?> = decimal("average_purchase", 12, 2).nullable()
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
}
