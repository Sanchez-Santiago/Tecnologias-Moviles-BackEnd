package com.misuper.backend.modules.shoppinglist.validators

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.shoppinglist.dto.AddProductRequest

object ShoppingListValidator {

    private val allowedPriorities = setOf("ESSENTIAL", "PRIMARY", "SECONDARY")
    private val allowedUnits = setOf("UNIT", "KG", "G", "L", "ML", "PACK")

    fun validateAddProduct(request: AddProductRequest) {
        request.priority?.let { p ->
            if (p.uppercase() !in allowedPriorities) {
                throw ValidationException("Prioridad inválida. Solo se permite: ESSENTIAL, PRIMARY, SECONDARY")
            }
        }
        request.unit?.let { u ->
            if (u.uppercase() !in allowedUnits) {
                throw ValidationException("Unidad inválida. Solo se permite: UNIT, KG, G, L, ML, PACK")
            }
        }
        request.estimatedPrice?.let { price ->
            if (price < 0) {
                throw ValidationException("El precio estimado no puede ser negativo")
            }
        }
        request.estimatedQuantity?.let { qty ->
            if (qty <= 0) {
                throw ValidationException("La cantidad estimada debe ser mayor a cero")
            }
        }
    }
}
