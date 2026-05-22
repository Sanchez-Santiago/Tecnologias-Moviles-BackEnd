package com.misuper.backend.modules.products.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProductRequest(
    val name: String? = null,
    val price: Double? = null,
    val categoryId: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val barcode: String? = null
)
