package com.misuper.backend.modules.auth.validators

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.auth.dto.LoginRequest
import com.misuper.backend.modules.auth.dto.RegisterRequest

object AuthValidator {

    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun validateRegisterRequest(request: RegisterRequest) {
        validateEmail(request.email)
        validatePassword(request.password)
        if (request.fullName.isBlank()) {
            throw ValidationException("El nombre completo es obligatorio")
        }
    }

    fun validateLoginRequest(request: LoginRequest) {
        validateEmail(request.email)
        if (request.password.isBlank()) {
            throw ValidationException("La contraseña es obligatoria")
        }
    }

    fun validateEmail(email: String) {
        if (email.isBlank()) {
            throw ValidationException("El email es obligatorio")
        }
        if (!emailRegex.matches(email)) {
            throw ValidationException("Formato de email inválido")
        }
    }

    fun validatePassword(password: String) {
        if (password.length < 8) {
            throw ValidationException("La contraseña debe tener al menos 8 caracteres")
        }
        if (!password.any { it.isUpperCase() }) {
            throw ValidationException("La contraseña debe contener al menos una mayúscula")
        }
        if (!password.any { it.isLowerCase() }) {
            throw ValidationException("La contraseña debe contener al menos una minúscula")
        }
        if (!password.any { it.isDigit() }) {
            throw ValidationException("La contraseña debe contener al menos un número")
        }
    }
}
