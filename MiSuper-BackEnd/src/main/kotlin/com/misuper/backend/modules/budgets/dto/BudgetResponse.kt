package com.misuper.backend.modules.budgets.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class BudgetResponse(
    val id: String,
    val groupId: String,
    val name: String,
    @Contextual val totalAmount: BigDecimal,
    val period: String,
    @Contextual val startDate: LocalDateTime,
    @Contextual val endDate: LocalDateTime? = null,
    val items: List<BudgetItemResponse>,
    @Contextual val createdAt: LocalDateTime
)

@Serializable
data class BudgetItemResponse(
    val id: String,
    val categoryId: String,
    val categoryName: String,
    @Contextual val amount: BigDecimal
)
