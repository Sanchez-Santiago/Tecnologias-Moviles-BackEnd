package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

object PasswordHistoryTable : UUIDTable("password_history") {
    val passwordHash: Column<String> = text("password_hash")
    val active: Column<Boolean> = bool("active").default(true)
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
    val expiredAt: Column<LocalDateTime?> = datetime("expired_at").nullable()
}
