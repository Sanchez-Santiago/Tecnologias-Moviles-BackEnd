package com.misuper.backend.modules.tickets.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateTicketRequest(
    val groupId: String,
    val title: String,
    val description: String,
    val priority: String? = "MEDIUM",
    val assignedTo: String? = null
)

@Serializable
data class UpdateTicketRequest(
    val title: String? = null,
    val description: String? = null,
    val status: String? = null,
    val priority: String? = null,
    val assignedTo: String? = null
)

@Serializable
data class AddMessageRequest(
    val message: String
)
