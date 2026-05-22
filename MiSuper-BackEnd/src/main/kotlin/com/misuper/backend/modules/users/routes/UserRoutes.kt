package com.misuper.backend.modules.users.routes

import com.misuper.backend.modules.users.dto.ChangePasswordRequest
import com.misuper.backend.modules.users.dto.UpdateProfileRequest
import com.misuper.backend.modules.users.dto.UpdateSettingsRequest
import com.misuper.backend.modules.users.services.UserService
import com.misuper.backend.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

class UserRoutes(private val userService: UserService) {

    fun register(routing: Route) {
        routing.route("users") {

            authenticate("auth-jwt") {
                get("me") {
                    val userId = call.principal<JWTPrincipal>()?.payload?.subject?.let { UUID.fromString(it) }
                        ?: throw IllegalArgumentException("Usuario no autenticado")
                    val profile = userService.getProfile(userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(profile))
                }

                put("me") {
                    val userId = call.principal<JWTPrincipal>()?.payload?.subject?.let { UUID.fromString(it) }
                        ?: throw IllegalArgumentException("Usuario no autenticado")
                    val request = call.receive<UpdateProfileRequest>()
                    val profile = userService.updateProfile(userId, request)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(profile))
                }

                put("me/password") {
                    val userId = call.principal<JWTPrincipal>()?.payload?.subject?.let { UUID.fromString(it) }
                        ?: throw IllegalArgumentException("Usuario no autenticado")
                    val request = call.receive<ChangePasswordRequest>()
                    userService.changePassword(userId, request)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(mapOf("message" to "Contraseña actualizada correctamente")))
                }

                get("me/settings") {
                    val userId = call.principal<JWTPrincipal>()?.payload?.subject?.let { UUID.fromString(it) }
                        ?: throw IllegalArgumentException("Usuario no autenticado")
                    val settings = userService.getSettings(userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(settings))
                }

                put("me/settings") {
                    val userId = call.principal<JWTPrincipal>()?.payload?.subject?.let { UUID.fromString(it) }
                        ?: throw IllegalArgumentException("Usuario no autenticado")
                    val request = call.receive<UpdateSettingsRequest>()
                    val settings = userService.updateSettings(userId, request)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(settings))
                }
            }
        }
    }
}
