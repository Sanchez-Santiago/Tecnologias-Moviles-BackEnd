package com.misuper.backend.modules.budgets.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateBudgetItem(
    val categoryId: String,
    val amount: Double
)

@Serializable
data class CreateBudgetRequest(
    val groupId: String,
    val name: String,
    val totalAmount: Double,
    val period: String,
    val startDate: String,
    val endDate: String? = null,
    val items: List<CreateBudgetItem>
)

@Serializable
data class UpdateBudgetRequest(
    val name: String? = null,
    val totalAmount: Double? = null,
    val period: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val items: List<CreateBudgetItem>? = null
)
