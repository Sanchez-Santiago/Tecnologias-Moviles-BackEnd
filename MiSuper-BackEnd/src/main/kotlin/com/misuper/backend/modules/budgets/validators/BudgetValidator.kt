package com.misuper.backend.modules.budgets.validators

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.budgets.dto.CreateBudgetRequest
import com.misuper.backend.modules.budgets.dto.UpdateBudgetRequest

object BudgetValidator {

    fun validateCreate(request: CreateBudgetRequest) {
        if (request.groupId.isBlank()) throw ValidationException("El grupo es obligatorio")
        if (request.name.isBlank()) throw ValidationException("El nombre es obligatorio")
        if (request.totalAmount <= 0) throw ValidationException("El monto total debe ser mayor a cero")
        val validPeriods = setOf("MONTHLY", "WEEKLY", "YEARLY", "CUSTOM")
        if (request.period.uppercase() !in validPeriods) {
            throw ValidationException("Período inválido. Use: MONTHLY, WEEKLY, YEARLY o CUSTOM")
        }
        if (request.items.isEmpty()) throw ValidationException("Debe incluir al menos una categoría")
        request.items.forEach { item ->
            if (item.categoryId.isBlank()) throw ValidationException("ID de categoría inválido")
            if (item.amount <= 0) throw ValidationException("El monto por categoría debe ser mayor a cero")
        }
    }

    fun validateUpdate(request: UpdateBudgetRequest) {
        if (request.name != null && request.name.isBlank()) {
            throw ValidationException("El nombre no puede estar vacío")
        }
        if (request.totalAmount != null && request.totalAmount <= 0) {
            throw ValidationException("El monto total debe ser mayor a cero")
        }
        if (request.period != null) {
            val validPeriods = setOf("MONTHLY", "WEEKLY", "YEARLY", "CUSTOM")
            if (request.period.uppercase() !in validPeriods) {
                throw ValidationException("Período inválido. Use: MONTHLY, WEEKLY, YEARLY o CUSTOM")
            }
        }
        if (request.items != null) {
            if (request.items.isEmpty()) throw ValidationException("Debe incluir al menos una categoría")
            request.items.forEach { item ->
                if (item.categoryId.isBlank()) throw ValidationException("ID de categoría inválido")
                if (item.amount <= 0) throw ValidationException("El monto por categoría debe ser mayor a cero")
            }
        }
    }
}
