package com.misuper.backend.modules.groups.validators

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.groups.dto.CreateGroupRequest

object GroupValidator {

    fun validateCreate(request: CreateGroupRequest) {
        if (request.name.isBlank()) {
            throw ValidationException("El nombre del grupo es obligatorio")
        }
    }
}
