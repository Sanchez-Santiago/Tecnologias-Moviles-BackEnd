package com.misuper.backend.modules.periods.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreatePeriodRequest(
    val name: String? = null,
    val cycleType: String = "MONTHLY"
)
