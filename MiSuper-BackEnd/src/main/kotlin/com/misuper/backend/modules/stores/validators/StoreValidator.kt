package com.misuper.backend.modules.stores.validators

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.stores.dto.CreateStoreRequest

object StoreValidator {

    fun validateCreate(request: CreateStoreRequest) {
        if (request.name.isBlank()) {
            throw ValidationException("El nombre de la tienda es obligatorio")
        }
    }
}
