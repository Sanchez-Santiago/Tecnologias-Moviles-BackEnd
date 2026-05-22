package com.misuper.backend.modules.users.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val fullName: String,
    val phone: String? = null,
    val alternativePhone: String? = null,
    val profilePictureUrl: String? = null
)
