package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

object UserSettingsTable : UUIDTable("user_settings") {
    val userId: Column<EntityID<UUID>> = reference("user_id", UsersTable).uniqueIndex()
    val language: Column<String> = varchar("language", 10).default("es")
    val notificationsEnabled: Column<Boolean> = bool("notifications_enabled").default(true)
    val currency: Column<String> = varchar("currency", 10).default("ARS")
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt: Column<LocalDateTime> = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
