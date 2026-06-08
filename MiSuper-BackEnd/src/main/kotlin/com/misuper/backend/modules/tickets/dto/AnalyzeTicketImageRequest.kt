package com.misuper.backend.modules.tickets.dto

import kotlinx.serialization.Serializable

@Serializable
data class AnalyzeTicketImageRequest(
    val imageBase64: String,
    val mimeType: String = "image/jpeg"
)

@Serializable
data class TicketProductDetection(
    val name: String,
    val quantity: Double? = null,
    val unitPrice: Double? = null,
    val totalPrice: Double? = null
)

@Serializable
data class AnalyzeTicketImageResponse(
    val storeName: String? = null,
    val purchaseDate: String? = null,
    val total: Double? = null,
    val products: List<TicketProductDetection>
)
