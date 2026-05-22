package com.misuper.backend.modules.stores.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateStoreRequest(
    val name: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
