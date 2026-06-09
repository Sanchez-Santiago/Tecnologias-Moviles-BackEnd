package com.misuper.backend.modules.groups.dto

import kotlinx.serialization.Serializable

@Serializable
data class InviteRequest(
    val email: String
)
