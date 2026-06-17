package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import java.util.UUID

object FavoriteProductsTable : UUIDTable("favorite_products") {
    val userId: Column<EntityID<UUID>> = reference("user_id", UsersTable)
    val productId: Column<EntityID<UUID>> = reference("product_id", ProductsTable)
}
