package com.misuper.backend.modules.groups.validators

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.groups.dto.CreateGroupRequest

object GroupValidator {

    private val validCategorias = setOf("FAMILIA", "AMIGOS", "TRABAJO", "INDIVIDUAL")

    fun validateCreate(request: CreateGroupRequest) {
        if (request.name.isBlank()) {
            throw ValidationException("El nombre del grupo es obligatorio")
        }
        request.categoria?.let { cat ->
            if (cat !in validCategorias) {
                throw ValidationException("Categoría inválida. Valores: ${validCategorias.joinToString(", ")}")
            }
        }
    }
}
