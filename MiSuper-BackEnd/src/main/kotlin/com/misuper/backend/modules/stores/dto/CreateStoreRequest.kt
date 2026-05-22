package com.misuper.backend.modules.stores.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateStoreRequest(
    val name: String,
    val address: String? = null,
    val phone: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
