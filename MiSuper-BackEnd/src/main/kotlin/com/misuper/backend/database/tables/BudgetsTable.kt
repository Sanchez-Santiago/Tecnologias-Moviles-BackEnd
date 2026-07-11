package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import org.jetbrains.exposed.v1.javatime.date
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

object BudgetsTable : UUIDTable("budgets") {
    val groupId: Column<EntityID<UUID>> = reference("group_id", GroupsTable)
    val startDate: Column<LocalDate> = date("start_date")
    val endDate: Column<LocalDate> = date("end_date")
    val total: Column<BigDecimal> = decimal("total", 12, 2)
    val createdBy: Column<EntityID<UUID>?> = reference("created_by", UsersTable).nullable()
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt: Column<LocalDateTime> = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
