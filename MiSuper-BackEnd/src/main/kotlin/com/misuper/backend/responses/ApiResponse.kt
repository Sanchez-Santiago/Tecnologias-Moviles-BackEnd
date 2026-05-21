package com.misuper.backend.responses

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(success = true, data = data)

        fun error(message: String, errorCode: String? = null): ApiResponse<Unit> =
            ApiResponse(success = false, message = message, errorCode = errorCode)
    }
}
