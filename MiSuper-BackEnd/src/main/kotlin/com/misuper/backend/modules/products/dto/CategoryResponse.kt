package com.misuper.backend.modules.products.dto

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class CategoryResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val active: Boolean
)
