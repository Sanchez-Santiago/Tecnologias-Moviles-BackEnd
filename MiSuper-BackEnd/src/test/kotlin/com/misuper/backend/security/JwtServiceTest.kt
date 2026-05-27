package com.misuper.backend.security

import kotlin.test.Test
import kotlin.test.assertEquals

class JwtServiceTest {
    @Test
    fun generatedAccessTokenContainsExpectedClaims() {
        val config = TokenConfig(
            secret = "test-secret-with-enough-length-for-hmac",
            issuer = "misuper-test",
            audience = "misuper-app-test",
            accessExpirationMinutes = 15,
            refreshExpirationDays = 7
        )
        val service = JwtService(config)
        val userId = java.util.UUID.randomUUID()

        val decoded = service.verifier().verify(service.generateAccessToken(userId, "test@mail.com", "ADMIN"))

        assertEquals(userId.toString(), decoded.subject)
        assertEquals("test@mail.com", decoded.getClaim("email").asString())
        assertEquals("ADMIN", decoded.getClaim("role").asString())
    }
}
