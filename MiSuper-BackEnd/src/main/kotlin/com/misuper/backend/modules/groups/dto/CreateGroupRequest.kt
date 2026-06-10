package com.misuper.backend.modules.groups.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    val categoria: String? = null
)
