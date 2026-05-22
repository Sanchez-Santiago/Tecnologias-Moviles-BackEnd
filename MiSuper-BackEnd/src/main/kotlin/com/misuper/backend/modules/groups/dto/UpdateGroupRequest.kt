package com.misuper.backend.modules.groups.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateGroupRequest(
    val name: String? = null,
    val description: String? = null
)
