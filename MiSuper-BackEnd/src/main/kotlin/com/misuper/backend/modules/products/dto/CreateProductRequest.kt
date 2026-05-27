package com.misuper.backend.modules.products.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateProductRequest(
    val name: String,
    val price: Double,
    val categoryId: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val barcode: String? = null,
    val priority: String = "SECUNDARIO"
)
