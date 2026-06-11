package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

object ShoppingListsTable : UUIDTable("shopping_lists") {
    val groupId: Column<EntityID<UUID>> = reference("group_id", GroupsTable)
    val createdBy: Column<EntityID<UUID>?> = reference("created_by", UsersTable).nullable()
    val name: Column<String> = varchar("name", 255)
    val description: Column<String?> = text("description").nullable()
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
}

object ShoppingListProductsTable : UUIDTable("shopping_list_products") {
    val shoppingListId: Column<EntityID<UUID>> = reference("shopping_list_id", ShoppingListsTable)
    val productId: Column<EntityID<UUID>> = reference("product_id", ProductsTable)
    val checked: Column<Boolean> = bool("checked").default(false)
    val finalPrice: Column<java.math.BigDecimal?> = decimal("final_price", 12, 2).nullable()
    val finalQuantity: Column<java.math.BigDecimal?> = decimal("final_quantity", 12, 2).nullable()
    val notes: Column<String?> = text("notes").nullable()
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
}
