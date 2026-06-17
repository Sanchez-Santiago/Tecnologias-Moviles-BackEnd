package com.misuper.backend.database.tables

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

@OptIn(ExperimentalUuidApi::class)
object AuditLogsTable : UUIDTable("audit_logs") {
    val userId: Column<EntityID<UUID>?> = reference("user_id", UsersTable).nullable()
    val action: Column<String> = varchar("action", 100)
    val entity: Column<String> = varchar("entity", 100)
    val entityId: Column<Uuid?> = uuid("entity_id").nullable()
    val description: Column<String?> = text("description").nullable()
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
    val groupId: Column<EntityID<UUID>?> = reference("group_id", GroupsTable).nullable()
}
