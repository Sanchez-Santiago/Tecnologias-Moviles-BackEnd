package com.misuper.backend.modules.stores.dto

import kotlinx.serialization.Serializable

@Serializable
data class StoreResponse(
    val id: String,
    val name: String,
    val address: String? = null,
    val phone: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val active: Boolean
)
