package com.misuper.backend.modules.periods.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class PeriodResponse(
    val id: String,
    val groupId: String,
    val name: String?,
    @Contextual val startDate: LocalDateTime,
    @Contextual val endDate: LocalDateTime?,
    val status: String,
    @Contextual val initialBalance: BigDecimal,
    @Contextual val finalBalance: BigDecimal?,
    @Contextual val createdAt: LocalDateTime,
    val closedBy: String?,
    @Contextual val updatedAt: LocalDateTime,
    val cycleType: String
)
