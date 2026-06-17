package com.misuper.backend.modules.periods.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.math.BigDecimal

@Serializable
data class ClosePeriodRequest(
    @Contextual val finalBalance: BigDecimal? = null
)
