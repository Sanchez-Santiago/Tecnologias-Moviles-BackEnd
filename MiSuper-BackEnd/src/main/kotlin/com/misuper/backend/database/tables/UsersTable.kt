package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

object UsersTable : UUIDTable("users") {
    val fullName: Column<String> = varchar("full_name", 255)
    val email: Column<String> = varchar("email", 255).uniqueIndex()
    val profilePictureUrl: Column<String?> = text("profile_picture_url").nullable()
    val phone: Column<String?> = varchar("phone", 50).nullable()
    val alternativePhone: Column<String?> = varchar("alternative_phone", 50).nullable()
    val role: Column<String> = varchar("role", 20).default("USER")
    val verified: Column<Boolean> = bool("verified").default(false)
    val failedAttempts: Column<Int> = integer("failed_attempts").default(0)
    val blocked: Column<Boolean> = bool("blocked").default(false)
    val passwordHash: Column<String> = text("password_hash")
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt: Column<LocalDateTime> = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
