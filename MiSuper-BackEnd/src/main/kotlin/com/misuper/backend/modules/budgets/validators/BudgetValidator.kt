package com.misuper.backend.modules.budgets.validators

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.budgets.dto.CreateBudgetRequest
import com.misuper.backend.modules.budgets.dto.UpdateBudgetRequest
import java.time.LocalDate

object BudgetValidator {

    fun validateCreate(request: CreateBudgetRequest) {
        if (request.groupId.isBlank()) throw ValidationException("El grupo es obligatorio")
        if (request.startDate.isBlank()) throw ValidationException("La fecha de inicio es obligatoria")
        if (request.endDate.isBlank()) throw ValidationException("La fecha de fin es obligatoria")
        if (request.total <= 0) throw ValidationException("El monto total debe ser mayor a cero")

        val start = LocalDate.parse(request.startDate)
        val end = LocalDate.parse(request.endDate)
        if (!end.isAfter(start)) throw ValidationException("La fecha de fin debe ser posterior a la de inicio")
    }

    fun validateUpdate(request: UpdateBudgetRequest) {
        if (request.total != null && request.total <= 0) {
            throw ValidationException("El monto total debe ser mayor a cero")
        }
        if (request.startDate != null && request.endDate != null) {
            val start = LocalDate.parse(request.startDate)
            val end = LocalDate.parse(request.endDate)
            if (!end.isAfter(start)) throw ValidationException("La fecha de fin debe ser posterior a la de inicio")
        }
    }
}
