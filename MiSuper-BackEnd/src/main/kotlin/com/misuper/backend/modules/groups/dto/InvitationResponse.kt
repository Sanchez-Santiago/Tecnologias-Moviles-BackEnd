package com.misuper.backend.modules.groups.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class InvitationResponse(
    val id: String,
    val groupId: String,
    val groupName: String,
    val invitedBy: String,
    val invitedByEmail: String,
    val status: String,
    val token: String,
    @Contextual val expiresAt: LocalDateTime,
    @Contextual val createdAt: LocalDateTime
)

@Serializable
data class AcceptRejectRequest(
    val token: String
)
