package com.misuper.backend.modules.auth.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class UserResponse(
    @Contextual val id: UUID,
    val fullName: String,
    val email: String,
    val role: String,
    val verified: Boolean,
    @Contextual val createdAt: LocalDateTime
)
