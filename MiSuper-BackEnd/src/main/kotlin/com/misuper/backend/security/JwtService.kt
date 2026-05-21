package com.misuper.backend.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.JWTAuthenticationProvider
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class JwtService(private val config: TokenConfig) {

    private val algorithm: Algorithm = Algorithm.HMAC256(config.secret)

    fun generateAccessToken(userId: UUID, email: String, role: String): String {
        val now = LocalDateTime.now()
        val expiration = now.plusMinutes(config.accessExpirationMinutes)

        return JWT.create()
            .withSubject(userId.toString())
            .withClaim("email", email)
            .withClaim("role", role)
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
            .withExpiresAt(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant()))
            .sign(algorithm)
    }

    fun generateRefreshToken(): Pair<String, LocalDateTime> {
        val now = LocalDateTime.now()
        val expiration = now.plusDays(config.refreshExpirationDays)
        val token = UUID.randomUUID().toString().replace("-", "") +
                UUID.randomUUID().toString().replace("-", "")

        return token to expiration
    }

    fun verifier(): JWTVerifier {
        return JWT.require(algorithm)
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .build()
    }
}
