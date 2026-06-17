package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

object BudgetItemsTable : UUIDTable("budget_items") {
    val budgetId: Column<EntityID<UUID>> = reference("budget_id", BudgetsTable)
    val categoryId: Column<EntityID<UUID>> = reference("category_id", CategoriesTable)
    val amount: Column<BigDecimal> = decimal("amount", 12, 2)
    val active: Column<Boolean> = bool("active").default(true)
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt: Column<LocalDateTime> = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
