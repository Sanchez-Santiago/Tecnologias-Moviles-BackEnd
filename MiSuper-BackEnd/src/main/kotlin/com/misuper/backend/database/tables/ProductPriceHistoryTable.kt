package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

object ProductPriceHistoryTable : UUIDTable("product_price_history") {
    val productId: Column<EntityID<UUID>> = reference("product_id", ProductsTable)
    val storeId: Column<EntityID<UUID>?> = reference("store_id", StoresTable).nullable()
    val price: Column<BigDecimal> = decimal("price", 12, 2)
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
}
