package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

object GroupMonthlyPeriodsTable : UUIDTable("group_monthly_periods") {
    val groupId: Column<EntityID<UUID>> = reference("group_id", GroupsTable)
    val name: Column<String?> = varchar("name", 255).nullable()
    val startDate: Column<LocalDateTime> = datetime("start_date")
    val endDate: Column<LocalDateTime?> = datetime("end_date").nullable()
    val status: Column<String> = varchar("status", 20).default("OPEN")
    val initialBalance: Column<BigDecimal> = decimal("initial_balance", 12, 2).default(BigDecimal.ZERO)
    val finalBalance: Column<BigDecimal?> = decimal("final_balance", 12, 2).nullable()
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
    val closedBy: Column<EntityID<UUID>?> = reference("closed_by", UsersTable).nullable()
    val updatedAt: Column<LocalDateTime> = datetime("updated_at").clientDefault { LocalDateTime.now() }
    val cycleType: Column<String> = varchar("cycle_type", 20).default("MONTHLY")
}
