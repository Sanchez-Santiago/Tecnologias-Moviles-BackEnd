package com.misuper.backend.modules.offers.validators

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.offers.dto.CreateOfferRequest
import com.misuper.backend.modules.offers.dto.UpdateOfferRequest

object OfferValidator {

    fun validateCreate(request: CreateOfferRequest) {
        if (request.title.isBlank()) throw ValidationException("El título es obligatorio")
        if (request.discountType.isBlank()) throw ValidationException("El tipo de descuento es obligatorio")
        val validTypes = setOf("PERCENTAGE", "FIXED_AMOUNT", "BUY_X_GET_Y")
        if (request.discountType.uppercase() !in validTypes) {
            throw ValidationException("Tipo de descuento inválido. Use: PERCENTAGE, FIXED_AMOUNT o BUY_X_GET_Y")
        }
        if (request.discountValue <= 0) throw ValidationException("El valor del descuento debe ser mayor a cero")
        if (request.startDate.isBlank()) throw ValidationException("La fecha de inicio es obligatoria")
        if (request.endDate.isBlank()) throw ValidationException("La fecha de fin es obligatoria")
    }

    fun validateUpdate(request: UpdateOfferRequest) {
        if (request.title != null && request.title.isBlank()) {
            throw ValidationException("El título no puede estar vacío")
        }
        if (request.discountType != null) {
            val validTypes = setOf("PERCENTAGE", "FIXED_AMOUNT", "BUY_X_GET_Y")
            if (request.discountType.uppercase() !in validTypes) {
                throw ValidationException("Tipo de descuento inválido")
            }
        }
        if (request.discountValue != null && request.discountValue <= 0) {
            throw ValidationException("El valor del descuento debe ser mayor a cero")
        }
    }
}
