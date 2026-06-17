package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import java.util.UUID

object RecurringProductsTable : UUIDTable("recurring_products") {
    val groupId: Column<EntityID<UUID>> = reference("group_id", GroupsTable)
    val productId: Column<EntityID<UUID>> = reference("product_id", ProductsTable)
    val frequency: Column<String> = varchar("frequency", 50)
    val active: Column<Boolean> = bool("active").default(true)
}
