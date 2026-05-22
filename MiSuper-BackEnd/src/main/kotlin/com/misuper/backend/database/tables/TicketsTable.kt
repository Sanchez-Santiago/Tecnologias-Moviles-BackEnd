package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

object TicketsTable : UUIDTable("tickets") {
    val groupId: Column<EntityID<UUID>> = reference("group_id", GroupsTable)
    val createdBy: Column<EntityID<UUID>> = reference("created_by", UsersTable)
    val title: Column<String> = varchar("title", 200)
    val description: Column<String> = text("description")
    val status: Column<String> = varchar("status", 20).default("OPEN")
    val priority: Column<String> = varchar("priority", 20).default("MEDIUM")
    val assignedTo: Column<EntityID<UUID>?> = reference("assigned_to", UsersTable).nullable()
    val active: Column<Boolean> = bool("active").default(true)
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt: Column<LocalDateTime> = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object TicketMessagesTable : UUIDTable("ticket_messages") {
    val ticketId: Column<EntityID<UUID>> = reference("ticket_id", TicketsTable)
    val userId: Column<EntityID<UUID>> = reference("user_id", UsersTable)
    val message: Column<String> = text("message")
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
}
