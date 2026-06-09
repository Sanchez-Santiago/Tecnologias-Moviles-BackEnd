package com.misuper.backend.modules.purchases.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePurchaseRequest(
    val storeId: String? = null,
    val notes: String? = null
)
