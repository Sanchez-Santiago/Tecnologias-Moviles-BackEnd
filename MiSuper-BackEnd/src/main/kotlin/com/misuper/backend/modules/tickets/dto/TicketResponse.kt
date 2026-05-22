package com.misuper.backend.modules.tickets.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class TicketResponse(
    val id: String,
    val groupId: String,
    val createdBy: String,
    val createdByName: String,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    val assignedTo: String? = null,
    val assignedToName: String? = null,
    val messages: List<TicketMessageResponse> = emptyList(),
    @Contextual val createdAt: LocalDateTime,
    @Contextual val updatedAt: LocalDateTime
)

@Serializable
data class TicketMessageResponse(
    val id: String,
    val userId: String,
    val userName: String,
    val message: String,
    @Contextual val createdAt: LocalDateTime
)
