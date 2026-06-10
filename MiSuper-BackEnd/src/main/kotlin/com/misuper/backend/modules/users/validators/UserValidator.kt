package com.misuper.backend.modules.users.validators

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.users.dto.ChangePasswordRequest
import com.misuper.backend.modules.users.dto.UpdateProfileRequest

object UserValidator {

    fun validateUpdateProfile(request: UpdateProfileRequest) {
        if (request.fullName.isBlank()) {
            throw ValidationException("El nombre completo es obligatorio")
        }
        if (request.fullName.length < 2) {
            throw ValidationException("El nombre debe tener al menos 2 caracteres")
        }
        request.email?.let { email ->
            if (email.isBlank()) {
                throw ValidationException("El email no puede estar vacío")
            }
            if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
                throw ValidationException("Formato de email inválido")
            }
        }
    }

    fun validateChangePassword(request: ChangePasswordRequest) {
        if (request.currentPassword.isBlank()) {
            throw ValidationException("La contraseña actual es obligatoria")
        }
        if (request.newPassword.isBlank()) {
            throw ValidationException("La nueva contraseña es obligatoria")
        }
        if (request.currentPassword == request.newPassword) {
            throw ValidationException("La nueva contraseña debe ser diferente a la actual")
        }
        validatePasswordStrength(request.newPassword)
    }

    private fun validatePasswordStrength(password: String) {
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
