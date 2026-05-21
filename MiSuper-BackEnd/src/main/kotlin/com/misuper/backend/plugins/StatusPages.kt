package com.misuper.backend.plugins

import com.misuper.backend.exceptions.*
import com.misuper.backend.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse.error(message = cause.message, errorCode = "VALIDATION_ERROR")
            )
        }

        exception<AuthException> { call, cause ->
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiResponse.error(message = cause.message, errorCode = "AUTH_ERROR")
            )
        }

        exception<ForbiddenException> { call, cause ->
            call.respond(
                HttpStatusCode.Forbidden,
                ApiResponse.error(message = cause.message, errorCode = "FORBIDDEN")
            )
        }

        exception<NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiResponse.error(message = cause.message, errorCode = "NOT_FOUND")
            )
        }

        exception<ConflictException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                ApiResponse.error(message = cause.message, errorCode = "CONFLICT")
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse.error(message = "Error interno del servidor", errorCode = "INTERNAL_ERROR")
            )
        }
    }
}
