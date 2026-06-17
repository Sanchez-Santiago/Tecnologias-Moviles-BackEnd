package com.misuper.backend.modules.periods.validators

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.periods.dto.CreatePeriodRequest

object PeriodValidator {

    private val validCycleTypes = setOf("MONTHLY", "BIWEEKLY", "WEEKLY", "CUSTOM")

    fun validateCreate(request: CreatePeriodRequest) {
        if (request.cycleType !in validCycleTypes) {
            throw ValidationException("Tipo de ciclo inválido. Valores: ${validCycleTypes.joinToString(", ")}")
        }
    }
}
