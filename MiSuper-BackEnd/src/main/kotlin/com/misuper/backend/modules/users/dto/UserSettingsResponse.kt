package com.misuper.backend.modules.users.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserSettingsResponse(
    val language: String,
    val notificationsEnabled: Boolean,
    val currency: String
)
