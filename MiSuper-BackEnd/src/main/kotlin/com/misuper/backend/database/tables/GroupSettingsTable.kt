package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

object GroupSettingsTable : UUIDTable("group_settings") {
    val groupId: Column<EntityID<UUID>> = reference("group_id", GroupsTable).uniqueIndex()
    val currency: Column<String> = varchar("currency", 10).default("ARS")
    val notificationsEnabled: Column<Boolean> = bool("notifications_enabled").default(true)
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
}
