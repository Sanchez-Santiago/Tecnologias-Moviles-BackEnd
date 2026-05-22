package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

object ProductsTable : UUIDTable("products") {
    val name: Column<String> = varchar("name", 255)
    val description: Column<String?> = text("description").nullable()
    val price: Column<BigDecimal> = decimal("price", 12, 2)
    val categoryId: Column<EntityID<UUID>> = reference("category_id", CategoriesTable)
    val imageUrl: Column<String?> = text("image_url").nullable()
    val barcode: Column<String?> = varchar("barcode", 100).nullable().uniqueIndex()
    val priority: Column<String> = varchar("priority", 20).default("SECUNDARIO")
    val active: Column<Boolean> = bool("active").default(true)
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt: Column<LocalDateTime> = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
