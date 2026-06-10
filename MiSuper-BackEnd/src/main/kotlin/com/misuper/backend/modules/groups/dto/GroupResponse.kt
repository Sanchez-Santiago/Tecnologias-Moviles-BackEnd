package com.misuper.backend.modules.groups.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class GroupResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val categoria: String = "FAMILIA",
    val createdBy: String,
    val memberCount: Int,
    val role: String,
    @Contextual val createdAt: LocalDateTime
)
