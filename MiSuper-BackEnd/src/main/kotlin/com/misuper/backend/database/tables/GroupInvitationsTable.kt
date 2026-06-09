package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

object GroupInvitationsTable : UUIDTable("group_invitations") {
    val groupId: Column<EntityID<UUID>> = reference("group_id", GroupsTable)
    val invitedEmail: Column<String> = varchar("invited_email", 255)
    val invitedBy: Column<EntityID<UUID>> = reference("invited_by", UsersTable)
    val token: Column<String> = varchar("token", 36).clientDefault { UUID.randomUUID().toString() }
    val status: Column<String> = varchar("status", 20).default("PENDING")
    val expiresAt: Column<LocalDateTime> = datetime("expires_at").clientDefault { LocalDateTime.now().plusDays(7) }
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt: Column<LocalDateTime> = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
