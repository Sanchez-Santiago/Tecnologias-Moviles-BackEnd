package com.misuper.backend.modules.groups.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class GroupDetailResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdBy: String,
    val members: List<GroupMemberResponse>,
    @Contextual val createdAt: LocalDateTime
)

@Serializable
data class GroupMemberResponse(
    val id: String,
    val fullName: String,
    val email: String,
    val role: String,
    @Contextual val joinedAt: LocalDateTime
)
