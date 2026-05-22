package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import java.math.BigDecimal
import java.util.UUID

object PurchaseProductsTable : UUIDTable("purchase_products") {
    val purchaseId: Column<EntityID<UUID>> = reference("purchase_id", PurchasesTable)
    val productId: Column<EntityID<UUID>> = reference("product_id", ProductsTable)
    val productName: Column<String> = varchar("product_name", 255)
    val quantity: Column<Int> = integer("quantity").default(1)
    val unitPrice: Column<BigDecimal> = decimal("unit_price", 12, 2)
    val subtotal: Column<BigDecimal> = decimal("subtotal", 12, 2)
}
