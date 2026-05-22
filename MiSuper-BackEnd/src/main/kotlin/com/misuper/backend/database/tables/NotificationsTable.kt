package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

object NotificationsTable : UUIDTable("notifications") {
    val userId: Column<EntityID<UUID>> = reference("user_id", UsersTable)
    val type: Column<String> = varchar("type", 50)
    val title: Column<String> = varchar("title", 200)
    val message: Column<String> = text("message")
    val data: Column<String?> = text("data").nullable()
    val read: Column<Boolean> = bool("read").default(false)
    val active: Column<Boolean> = bool("active").default(true)
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
}
