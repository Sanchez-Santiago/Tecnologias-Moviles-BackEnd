package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

object CategoriesTable : UUIDTable("categories") {
    val name: Column<String> = varchar("name", 100).uniqueIndex()
    val description: Column<String?> = text("description").nullable()
    val icon: Column<String?> = varchar("icon", 50).nullable()
    val active: Column<Boolean> = bool("active").default(true)
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt: Column<LocalDateTime> = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
