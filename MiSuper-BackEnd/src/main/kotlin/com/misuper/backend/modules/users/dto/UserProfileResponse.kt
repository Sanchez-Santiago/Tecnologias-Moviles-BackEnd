package com.misuper.backend.modules.users.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class UserProfileResponse(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val alternativePhone: String? = null,
    val profilePictureUrl: String? = null,
    val role: String,
    val verified: Boolean,
    @Contextual val createdAt: LocalDateTime
)
