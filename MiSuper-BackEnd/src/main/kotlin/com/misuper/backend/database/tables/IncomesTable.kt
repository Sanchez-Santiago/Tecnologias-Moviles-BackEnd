package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

object IncomesTable : UUIDTable("incomes") {
    val groupId: Column<EntityID<UUID>> = reference("group_id", GroupsTable)
    val periodId: Column<EntityID<UUID>> = reference("period_id", GroupMonthlyPeriodsTable)
    val amount: Column<BigDecimal> = decimal("amount", 12, 2)
    val description: Column<String?> = text("description").nullable()
    val date: Column<LocalDateTime> = datetime("date").clientDefault { LocalDateTime.now() }
    val createdBy: Column<EntityID<UUID>> = reference("created_by", UsersTable)
}
