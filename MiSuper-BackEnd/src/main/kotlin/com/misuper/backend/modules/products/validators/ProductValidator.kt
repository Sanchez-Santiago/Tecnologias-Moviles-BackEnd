package com.misuper.backend.modules.products.validators

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.products.dto.CreateCategoryRequest
import com.misuper.backend.modules.products.dto.CreateProductRequest
import com.misuper.backend.modules.products.dto.UpdateProductRequest
import java.util.UUID

object ProductValidator {

    fun validateCreateProduct(request: CreateProductRequest) {
        if (request.name.isBlank()) {
            throw ValidationException("El nombre del producto es obligatorio")
        }
        if (request.price <= 0) {
            throw ValidationException("El precio debe ser mayor a cero")
        }
        if (request.categoryId.isBlank()) {
            throw ValidationException("La categoría es obligatoria")
        }
        try {
            UUID.fromString(request.categoryId)
        } catch (e: IllegalArgumentException) {
            throw ValidationException("ID de categoría inválido")
        }
        validatePriority(request.priority)
    }

    fun validateUpdateProduct(request: UpdateProductRequest) {
        request.categoryId?.let {
            try {
                UUID.fromString(it)
            } catch (e: IllegalArgumentException) {
                throw ValidationException("ID de categoría inválido")
            }
        }
        request.price?.let {
            if (it <= 0) throw ValidationException("El precio debe ser mayor a cero")
        }
        request.priority?.let { validatePriority(it) }
    }

    fun validateCreateCategory(request: CreateCategoryRequest) {
        if (request.name.isBlank()) {
            throw ValidationException("El nombre de la categoría es obligatorio")
        }
    }

    private fun validatePriority(priority: String) {
        val allowed = setOf("ESENCIAL", "PRIMARIO", "SECUNDARIO")
        if (priority.uppercase() !in allowed) {
            throw ValidationException("La prioridad debe ser ESENCIAL, PRIMARIO o SECUNDARIO")
        }
    }
}
