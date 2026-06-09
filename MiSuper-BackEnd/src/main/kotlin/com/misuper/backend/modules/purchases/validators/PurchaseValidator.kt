package com.misuper.backend.modules.purchases.validators

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.purchases.dto.CreatePurchaseRequest
import com.misuper.backend.modules.purchases.dto.UpdatePurchaseRequest

object PurchaseValidator {

    fun validateCreate(request: CreatePurchaseRequest) {
        if (request.groupId.isBlank()) {
            throw ValidationException("El grupo es obligatorio")
        }
        if (request.items.isEmpty()) {
            throw ValidationException("Debe incluir al menos un producto")
        }
        request.items.forEach { item ->
            if (item.productId.isBlank()) {
                throw ValidationException("ID de producto inválido")
            }
            if (item.quantity < 1) {
                throw ValidationException("La cantidad debe ser mayor a cero")
            }
        }
    }

    fun validateUpdate(request: UpdatePurchaseRequest) {
        // All fields are optional; no required validation for now
    }
}
