package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

object ShoppingListsTable : UUIDTable("shopping_lists") {
    val groupId: Column<EntityID<UUID>> = reference("group_id", GroupsTable)
    val createdBy: Column<EntityID<UUID>?> = reference("created_by", UsersTable).nullable()
    val name: Column<String> = varchar("name", 255)
    val description: Column<String?> = text("description").nullable()
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
}

object ShoppingListItemsTable : UUIDTable("shopping_list_items") {
    val shoppingListId: Column<EntityID<UUID>> = reference("shopping_list_id", ShoppingListsTable)
    val productId: Column<EntityID<UUID>?> = reference("product_id", ProductsTable).nullable()
    val customProductName: Column<String?> = varchar("custom_product_name", 255).nullable()
    val estimatedPrice: Column<BigDecimal?> = decimal("estimated_price", 12, 2).nullable()
    val estimatedQuantity: Column<BigDecimal> = decimal("estimated_quantity", 12, 2).default(BigDecimal.ONE)
    val estimatedBrand: Column<String?> = varchar("estimated_brand", 255).nullable()
    val unit: Column<String?> = varchar("unit", 20).nullable()
    val priority: Column<String> = varchar("priority", 20).default("PRIMARY")
    val checked: Column<Boolean> = bool("checked").default(false)
    val notes: Column<String?> = text("notes").nullable()
    val createdBy: Column<EntityID<UUID>?> = reference("created_by", UsersTable).nullable()
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt: Column<LocalDateTime> = datetime("updated_at").clientDefault { LocalDateTime.now() }
    val lastCheckedAt: Column<LocalDateTime?> = datetime("last_checked_at").nullable()
}
