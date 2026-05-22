package com.misuper.backend.modules.users.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateSettingsRequest(
    val language: String? = null,
    val notificationsEnabled: Boolean? = null,
    val currency: String? = null
)
