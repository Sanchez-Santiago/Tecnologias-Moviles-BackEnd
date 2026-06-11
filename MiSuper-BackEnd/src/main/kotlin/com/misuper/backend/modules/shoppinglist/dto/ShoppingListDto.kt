package com.misuper.backend.modules.shoppinglist.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class CreateShoppingListRequest(
    val groupId: String,
    val name: String,
    val description: String? = null
)

@Serializable
data class UpdateShoppingListRequest(
    val name: String? = null,
    val description: String? = null
)

@Serializable
data class AddProductRequest(
    val productId: String,
    val quantity: Double? = null,
    val notes: String? = null
)

@Serializable
data class UpdateProductRequest(
    val checked: Boolean? = null,
    val finalPrice: Double? = null,
    val finalQuantity: Double? = null,
    val notes: String? = null
)

@Serializable
data class ShoppingListResponse(
    val id: String,
    val groupId: String,
    val createdBy: String? = null,
    val name: String,
    val description: String? = null,
    val products: List<ShoppingListProductResponse>,
    @Contextual val createdAt: LocalDateTime
)

@Serializable
data class ShoppingListProductResponse(
    val id: String,
    val productId: String,
    val productName: String,
    val checked: Boolean = false,
    val finalPrice: Double? = null,
    val finalQuantity: Double? = null,
    val notes: String? = null
)
