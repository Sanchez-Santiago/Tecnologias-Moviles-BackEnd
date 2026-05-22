package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

object GroupMembersTable : UUIDTable("group_members") {
    val groupId: Column<EntityID<UUID>> = reference("group_id", GroupsTable)
    val userId: Column<EntityID<UUID>> = reference("user_id", UsersTable)
    val role: Column<String> = varchar("role", 20).default("MEMBER")
    val joinedAt: Column<LocalDateTime> = datetime("joined_at").clientDefault { LocalDateTime.now() }
}
