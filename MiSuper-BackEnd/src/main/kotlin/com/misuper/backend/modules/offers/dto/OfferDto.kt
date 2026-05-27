package com.misuper.backend.modules.offers.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class CreateOfferRequest(
    val storeId: String? = null,
    val title: String,
    val description: String? = null,
    val discountType: String,
    val discountValue: Double,
    val startDate: String,
    val endDate: String,
    val imageUrl: String? = null,
    val termsConditions: String? = null
)

@Serializable
data class UpdateOfferRequest(
    val storeId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val discountType: String? = null,
    val discountValue: Double? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val imageUrl: String? = null,
    val termsConditions: String? = null
)

@Serializable
data class OfferResponse(
    val id: String,
    val storeId: String? = null,
    val storeName: String? = null,
    val title: String,
    val description: String? = null,
    val discountType: String,
    @Contextual val discountValue: BigDecimal,
    @Contextual val startDate: LocalDateTime,
    @Contextual val endDate: LocalDateTime,
    val imageUrl: String? = null,
    val termsConditions: String? = null,
    @Contextual val createdAt: LocalDateTime
)

@Serializable
data class MatchedOfferResponse(
    val productId: String,
    val productName: String,
    val offer: OfferResponse,
    val reason: String
)
