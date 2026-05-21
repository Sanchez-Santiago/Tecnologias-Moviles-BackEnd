package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

object LoginHistoryTable : UUIDTable("login_history") {
    val userId: Column<EntityID<UUID>> = reference("user_id", UsersTable)
    val ipAddress: Column<String?> = varchar("ip_address", 45).nullable()
    val userAgent: Column<String?> = text("user_agent").nullable()
    val success: Column<Boolean> = bool("success").default(true)
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
}
