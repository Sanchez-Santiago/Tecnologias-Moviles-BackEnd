package com.misuper.backend.modules.products.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class ProductResponse(
    val id: String,
    val name: String,
    @Contextual val price: BigDecimal,
    val categoryId: String,
    val categoryName: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val barcode: String? = null,
    val active: Boolean
)
