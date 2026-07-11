package com.misuper.backend.modules.budgets.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateBudgetRequest(
    val groupId: String,
    val startDate: String,
    val endDate: String,
    val total: Double
)

@Serializable
data class UpdateBudgetRequest(
    val total: Double? = null,
    val startDate: String? = null,
    val endDate: String? = null
)
