package com.misuper.backend.plugins

import com.misuper.backend.exceptions.*
import com.misuper.backend.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.slf4j.LoggerFactory

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

        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse.error(message = cause.message ?: "Solicitud inválida", errorCode = "BAD_REQUEST")
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse.error(message = cause.message ?: "Parámetros inválidos", errorCode = "BAD_REQUEST")
            )
        }

        exception<SerializationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse.error(message = "JSON inválido o incompleto", errorCode = "BAD_REQUEST")
            )
        }

        exception<ExposedSQLException> { call, cause ->
            val log = LoggerFactory.getLogger("StatusPages")
            log.error("Database constraint violation", cause)
            val msg = cause.message ?: ""
            val message = when {
                msg.contains("shopping_list_items_priority_check") ->
                    "Valor inválido para 'prioridad'. Solo se permite: ESSENTIAL, PRIMARY, SECONDARY"
                msg.contains("shopping_list_items_unit_check") ->
                    "Valor inválido para 'unidad'. Solo se permite: UNIT, KG, G, L, ML, PACK"
                msg.contains("shopping_list_items_estimated_price_check") ->
                    "El precio estimado no puede ser negativo"
                msg.contains("shopping_list_items_estimated_quantity_check") ->
                    "La cantidad estimada debe ser mayor a cero"
                msg.contains("groups_type_check") ->
                    "Valor inválido para 'tipo de grupo'. Solo se permite: PERSONAL, FAMILY, WORK, FRIENDS, OTHER"
                msg.contains("groups_cycle_day_check") ->
                    "El día del ciclo debe estar entre 1 y 28"
                msg.contains("tickets_movement_type_check") ->
                    "Valor inválido para 'tipo de movimiento'. Solo se permite: INCOME, EXPENSE"
                msg.contains("products_default_unit_check") ->
                    "Valor inválido para 'unidad por defecto'. Solo se permite: UNIT, KG, G, L, ML, PACK"
                msg.contains("chk_product_name") ->
                    "Debe proporcionar un producto existente o un nombre personalizado"
                msg.contains("chk_name") ->
                    "El nombre completo debe tener al menos 3 caracteres"
                msg.contains("chk_failed_attempts") ->
                    "El número de intentos fallidos no puede ser negativo"
                else -> {
                    log.error("Unhandled SQL constraint violation", cause)
                    "Error de validación en la base de datos"
                }
            }
            call.respond(HttpStatusCode.BadRequest, ApiResponse.error(message = message, errorCode = "VALIDATION_ERROR"))
        }

        exception<Throwable> { call, cause ->
            val log = LoggerFactory.getLogger("StatusPages")
            log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse.error(message = "Error interno del servidor", errorCode = "INTERNAL_ERROR")
            )
        }
    }
}
