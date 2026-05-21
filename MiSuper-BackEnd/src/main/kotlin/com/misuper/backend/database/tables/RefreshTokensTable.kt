package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

object RefreshTokensTable : UUIDTable("refresh_tokens") {
    val userId: Column<EntityID<UUID>> = reference("user_id", UsersTable)
    val token: Column<String> = text("token").uniqueIndex()
    val expiresAt: Column<LocalDateTime> = datetime("expires_at")
    val used: Column<Boolean> = bool("used").default(false)
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
}
