package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

object TicketsTable : UUIDTable("tickets") {
    val groupId: Column<EntityID<UUID>> = reference("group_id", GroupsTable)
    val uploadedBy: Column<EntityID<UUID>> = reference("uploaded_by", UsersTable)
    val supermarketName: Column<String> = varchar("supermarket_name", 255)
    val amount: Column<BigDecimal> = decimal("amount", 12, 2)
    val movementType: Column<String?> = varchar("movement_type", 20).nullable()
    val purchaseDate: Column<LocalDateTime> = datetime("purchase_date")
    val imageUrl: Column<String?> = text("image_url").nullable()
    val comment: Column<String?> = text("comment").nullable()
    val status: Column<String> = varchar("status", 20).default("PENDING")
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt: Column<LocalDateTime> = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object TicketProductsTable : UUIDTable("ticket_products") {
    val ticketId: Column<EntityID<UUID>> = reference("ticket_id", TicketsTable)
    val productId: Column<EntityID<UUID>?> = reference("product_id", ProductsTable).nullable()
    val detectedName: Column<String?> = varchar("detected_name", 255).nullable()
    val quantity: Column<BigDecimal?> = decimal("quantity", 12, 2).nullable()
    val price: Column<BigDecimal?> = decimal("price", 12, 2).nullable()
    val brand: Column<String?> = varchar("brand", 255).nullable()
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
}
