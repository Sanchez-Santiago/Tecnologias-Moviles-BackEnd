package com.misuper.backend.modules.budgets.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class BudgetResponse(
    val id: String,
    val groupId: String,
    val startDate: String,
    val endDate: String,
    @Contextual val total: BigDecimal,
    val createdBy: String? = null,
    @Contextual val createdAt: LocalDateTime,
    @Contextual val updatedAt: LocalDateTime
)
