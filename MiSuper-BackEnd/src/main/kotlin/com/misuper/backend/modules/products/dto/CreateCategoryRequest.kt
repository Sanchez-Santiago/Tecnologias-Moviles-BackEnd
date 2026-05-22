package com.misuper.backend.modules.products.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val description: String? = null,
    val icon: String? = null
)
