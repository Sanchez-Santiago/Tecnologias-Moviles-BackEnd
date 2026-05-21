package com.misuper.backend.modules.auth.services

import com.misuper.backend.database.tables.RefreshTokensTable
import com.misuper.backend.database.tables.UsersTable
import com.misuper.backend.exceptions.AuthException
import com.misuper.backend.exceptions.ConflictException
import com.misuper.backend.modules.auth.dto.*
import com.misuper.backend.modules.auth.repositories.AuthRepository
import com.misuper.backend.modules.auth.validators.AuthValidator
import com.misuper.backend.security.JwtService
import com.misuper.backend.security.PasswordHasher
import java.time.LocalDateTime

class AuthService(
    private val authRepository: AuthRepository,
    private val jwtService: JwtService,
    private val passwordHasher: PasswordHasher
) {
    fun register(request: RegisterRequest, ipAddress: String?, userAgent: String?): AuthResponse {
        AuthValidator.validateRegisterRequest(request)

        val existingUser = authRepository.findByEmail(request.email.lowercase())
        if (existingUser != null) {
            throw ConflictException("El email ya está registrado")
        }

        val passwordHash = passwordHasher.hash(request.password)
        val userId = authRepository.createUser(request.fullName, request.email.lowercase(), passwordHash)

        val accessToken = jwtService.generateAccessToken(userId, request.email.lowercase(), "USER")
        val (refreshToken, refreshExpiresAt) = jwtService.generateRefreshToken()
        authRepository.saveRefreshToken(userId, refreshToken, refreshExpiresAt)

        authRepository.logLoginAttempt(userId, ipAddress, userAgent, successVal = true)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = UserResponse(
                id = userId,
                fullName = request.fullName,
                email = request.email.lowercase(),
                role = "USER",
                verified = false,
                createdAt = LocalDateTime.now()
            )
        )
    }

    fun login(request: LoginRequest, ipAddress: String?, userAgent: String?): AuthResponse {
        AuthValidator.validateLoginRequest(request)

        val row = authRepository.findByEmail(request.email.lowercase())
            ?: throw AuthException("Credenciales inválidas")

        val userId = row[UsersTable.id].value
        val isBlocked = row[UsersTable.blocked]

        if (isBlocked) {
            authRepository.logLoginAttempt(userId, ipAddress, userAgent, successVal = false)
            throw AuthException("Usuario bloqueado. Contacte al administrador")
        }

        val passwordHash = row[UsersTable.passwordHash]
        if (!passwordHasher.verify(request.password, passwordHash)) {
            authRepository.incrementFailedAttempts(userId)
            authRepository.logLoginAttempt(userId, ipAddress, userAgent, successVal = false)
            throw AuthException("Credenciales inválidas")
        }

        authRepository.resetFailedAttempts(userId)

        val email = row[UsersTable.email]
        val role = row[UsersTable.role]
        val fullName = row[UsersTable.fullName]
        val verified = row[UsersTable.verified]
        val createdAt = row[UsersTable.createdAt]

        val accessToken = jwtService.generateAccessToken(userId, email, role)
        val (refreshToken, refreshExpiresAt) = jwtService.generateRefreshToken()
        authRepository.saveRefreshToken(userId, refreshToken, refreshExpiresAt)

        authRepository.logLoginAttempt(userId, ipAddress, userAgent, successVal = true)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = UserResponse(
                id = userId,
                fullName = fullName,
                email = email,
                role = role,
                verified = verified,
                createdAt = createdAt
            )
        )
    }

    fun refreshToken(refreshToken: String): AuthResponse {
        val tokenRow = authRepository.findRefreshToken(refreshToken)
            ?: throw AuthException("Refresh token inválido o expirado")

        val expiresAt = tokenRow[RefreshTokensTable.expiresAt]
        if (expiresAt.isBefore(LocalDateTime.now())) {
            authRepository.markRefreshTokenAsUsed(tokenRow[RefreshTokensTable.id].value)
            throw AuthException("Refresh token expirado")
        }

        val tokenId = tokenRow[RefreshTokensTable.id].value
        val userId = tokenRow[RefreshTokensTable.userId].value

        authRepository.markRefreshTokenAsUsed(tokenId)

        val userRow = authRepository.findById(userId)
            ?: throw AuthException("Usuario no encontrado")

        val email = userRow[UsersTable.email]
        val role = userRow[UsersTable.role]
        val fullName = userRow[UsersTable.fullName]
        val verified = userRow[UsersTable.verified]
        val createdAt = userRow[UsersTable.createdAt]

        val newAccessToken = jwtService.generateAccessToken(userId, email, role)
        val (newRefreshToken, refreshExpiresAt) = jwtService.generateRefreshToken()
        authRepository.saveRefreshToken(userId, newRefreshToken, refreshExpiresAt)

        return AuthResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            user = UserResponse(
                id = userId,
                fullName = fullName,
                email = email,
                role = role,
                verified = verified,
                createdAt = createdAt
            )
        )
    }
}
