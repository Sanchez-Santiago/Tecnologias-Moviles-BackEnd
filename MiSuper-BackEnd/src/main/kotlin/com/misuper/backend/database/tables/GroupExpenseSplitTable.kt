package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import java.math.BigDecimal
import java.util.UUID

object GroupExpenseSplitTable : UUIDTable("group_expense_split") {
    val purchaseId: Column<EntityID<UUID>> = reference("purchase_id", PurchasesTable)
    val userId: Column<EntityID<UUID>> = reference("user_id", UsersTable)
    val amount: Column<BigDecimal> = decimal("amount", 12, 2)
    val paid: Column<Boolean> = bool("paid").default(false)
}
