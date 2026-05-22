package com.misuper.backend.modules.notifications.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class NotificationResponse(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val data: String? = null,
    val read: Boolean,
    @Contextual val createdAt: LocalDateTime
)
