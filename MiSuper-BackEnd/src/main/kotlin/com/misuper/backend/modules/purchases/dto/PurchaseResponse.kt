package com.misuper.backend.modules.purchases.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class PurchaseResponse(
    val id: String,
    val groupId: String,
    val storeId: String? = null,
    val storeName: String? = null,
    val userId: String,
    val userName: String,
    @Contextual val total: BigDecimal,
    val notes: String? = null,
    val items: List<PurchaseProductResponse>,
    @Contextual val purchaseDate: LocalDateTime,
    @Contextual val createdAt: LocalDateTime
)

@Serializable
data class PurchaseProductResponse(
    val id: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    @Contextual val unitPrice: BigDecimal,
    @Contextual val subtotal: BigDecimal
)

@Serializable
data class PurchaseShareResponse(
    val purchaseId: String,
    val text: String
)
