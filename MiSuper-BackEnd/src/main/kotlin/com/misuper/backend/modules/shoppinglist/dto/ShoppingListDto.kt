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
    val productId: String? = null,
    val customProductName: String? = null,
    val estimatedPrice: Double? = null,
    val estimatedQuantity: Double? = null,
    val estimatedBrand: String? = null,
    val unit: String? = null,
    val priority: String? = null,
    val notes: String? = null
)

@Serializable
data class UpdateProductRequest(
    val checked: Boolean? = null,
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
    val productId: String? = null,
    val productName: String = "Producto",
    val customProductName: String? = null,
    val estimatedPrice: Double? = null,
    val estimatedQuantity: Double? = null,
    val estimatedBrand: String? = null,
    val unit: String? = null,
    val priority: String = "PRIMARY",
    val checked: Boolean = false,
    val notes: String? = null,
    @Contextual val lastCheckedAt: LocalDateTime? = null
)
