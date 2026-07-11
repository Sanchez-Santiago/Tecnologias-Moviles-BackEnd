package com.misuper.backend.modules.tickets.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class CreateTicketRequest(
    val groupId: String,
    val supermarketName: String,
    val movementType: String,
    val amount: Double,
    val purchaseDate: String? = null,
    val comment: String? = null,
    val imageUrl: String? = null,
    val products: List<CreateTicketProductRequest>? = null
)

@Serializable
data class CreateTicketProductRequest(
    val productId: String? = null,
    val productName: String? = null,
    val quantity: Double? = null,
    val price: Double? = null,
    val brand: String? = null
)

@Serializable
data class UpdateTicketRequest(
    val supermarketName: String? = null,
    val comment: String? = null,
    val imageUrl: String? = null
)

@Serializable
data class TicketResponse(
    val id: String,
    val groupId: String,
    val uploadedBy: String,
    val uploadedByName: String = "",
    val supermarketName: String,
    val amount: Double,
    val movementType: String?,
    val purchaseDate: String,
    val comment: String? = null,
    val imageUrl: String? = null,
    val status: String = "PENDING",
    val products: List<TicketProductResponse> = emptyList(),
    @Contextual val createdAt: LocalDateTime,
    @Contextual val updatedAt: LocalDateTime
)

@Serializable
data class TicketProductResponse(
    val id: String,
    val productId: String? = null,
    val productName: String? = null,
    val quantity: Double? = null,
    val price: Double? = null,
    val brand: String? = null
)
