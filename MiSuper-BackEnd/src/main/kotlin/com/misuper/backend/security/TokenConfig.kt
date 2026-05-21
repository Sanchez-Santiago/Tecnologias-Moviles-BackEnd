package com.misuper.backend.security

data class TokenConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val accessExpirationMinutes: Long,
    val refreshExpirationDays: Long
)
