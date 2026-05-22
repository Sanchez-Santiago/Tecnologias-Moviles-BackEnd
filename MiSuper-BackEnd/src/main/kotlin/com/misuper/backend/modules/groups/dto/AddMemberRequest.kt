package com.misuper.backend.modules.groups.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddMemberRequest(
    val email: String
)
