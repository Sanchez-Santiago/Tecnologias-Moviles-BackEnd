package com.misuper.backend.modules.users.services

import com.misuper.backend.database.tables.UserSettingsTable
import com.misuper.backend.database.tables.UsersTable
import com.misuper.backend.exceptions.AuthException
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.modules.users.dto.*
import com.misuper.backend.modules.users.repositories.UserRepository
import com.misuper.backend.modules.users.validators.UserValidator
import com.misuper.backend.security.PasswordHasher
import java.time.LocalDateTime
import java.util.UUID

class UserService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val passwordHistorySize: Int
) {
    fun getProfile(userId: UUID): UserProfileResponse {
        val row = userRepository.findById(userId)
            ?: throw NotFoundException("Usuario no encontrado")

        return UserProfileResponse(
            id = row[UsersTable.id].value.toString(),
            fullName = row[UsersTable.fullName],
            email = row[UsersTable.email],
            phone = row[UsersTable.phone],
            alternativePhone = row[UsersTable.alternativePhone],
            profilePictureUrl = row[UsersTable.profilePictureUrl],
            role = row[UsersTable.role],
            verified = row[UsersTable.verified],
            createdAt = row[UsersTable.createdAt]
        )
    }

    fun updateProfile(userId: UUID, request: UpdateProfileRequest): UserProfileResponse {
        UserValidator.validateUpdateProfile(request)

        val existing = userRepository.findById(userId)
            ?: throw NotFoundException("Usuario no encontrado")

        userRepository.updateProfile(
            userId = userId,
            fullNameVal = request.fullName,
            phoneVal = request.phone,
            alternativePhoneVal = request.alternativePhone,
            profilePictureUrlVal = request.profilePictureUrl
        )

        return UserProfileResponse(
            id = existing[UsersTable.id].value.toString(),
            fullName = request.fullName,
            email = existing[UsersTable.email],
            phone = request.phone,
            alternativePhone = request.alternativePhone,
            profilePictureUrl = request.profilePictureUrl,
            role = existing[UsersTable.role],
            verified = existing[UsersTable.verified],
            createdAt = existing[UsersTable.createdAt]
        )
    }

    fun changePassword(userId: UUID, request: ChangePasswordRequest) {
        UserValidator.validateChangePassword(request)

        val currentHash = userRepository.getPasswordHash(userId)
            ?: throw NotFoundException("Usuario no encontrado")

        if (!passwordHasher.verify(request.currentPassword, currentHash)) {
            throw AuthException("La contraseña actual es incorrecta")
        }

        val repeatedPassword = userRepository.getPasswordHistory(userId, passwordHistorySize)
            .any { previousHash -> passwordHasher.verify(request.newPassword, previousHash) }
        if (repeatedPassword) {
            throw AuthException("No puedes reutilizar una de tus últimas $passwordHistorySize contraseñas")
        }

        val newHash = passwordHasher.hash(request.newPassword)
        userRepository.updatePassword(userId, newHash)
    }

    fun getSettings(userId: UUID): UserSettingsResponse {
        userRepository.findById(userId)
            ?: throw NotFoundException("Usuario no encontrado")

        userRepository.initSettings(userId)

        val row = userRepository.getSettings(userId)
            ?: throw NotFoundException("Configuración no encontrada")

        return UserSettingsResponse(
            language = row[UserSettingsTable.language],
            notificationsEnabled = row[UserSettingsTable.notificationsEnabled],
            currency = row[UserSettingsTable.currency]
        )
    }

    fun updateSettings(userId: UUID, request: UpdateSettingsRequest): UserSettingsResponse {
        val existing = userRepository.findById(userId)
            ?: throw NotFoundException("Usuario no encontrado")

        userRepository.initSettings(userId)
        userRepository.updateSettings(
            userId = userId,
            languageVal = request.language,
            notificationsEnabledVal = request.notificationsEnabled,
            currencyVal = request.currency
        )

        val row = userRepository.getSettings(userId)!!
        return UserSettingsResponse(
            language = row[UserSettingsTable.language],
            notificationsEnabled = row[UserSettingsTable.notificationsEnabled],
            currency = row[UserSettingsTable.currency]
        )
    }
}
