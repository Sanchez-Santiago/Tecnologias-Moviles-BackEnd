package com.misuper.backend.modules.purchases.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreatePurchaseItem(
    val productId: String,
    val quantity: Int = 1
)

@Serializable
data class CreatePurchaseRequest(
    val groupId: String,
    val storeId: String? = null,
    val notes: String? = null,
    val items: List<CreatePurchaseItem>
)
