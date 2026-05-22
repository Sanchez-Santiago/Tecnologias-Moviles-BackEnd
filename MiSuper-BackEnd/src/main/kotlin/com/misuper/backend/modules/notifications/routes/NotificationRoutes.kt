package com.misuper.backend.modules.notifications.routes

import com.misuper.backend.modules.notifications.services.NotificationService
import com.misuper.backend.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

class NotificationRoutes(private val notificationService: NotificationService) {

    fun register(routing: Route) {
        routing.route("notifications") {

            authenticate("auth-jwt") {
                get {
                    val userId = userId(call)
                    val notifications = notificationService.getByUser(userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(notifications))
                }

                get("unread/count") {
                    val userId = userId(call)
                    val count = notificationService.getUnreadCount(userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(mapOf("count" to count)))
                }

                put("{id}/read") {
                    val userId = userId(call)
                    val notifId = UUID.fromString(call.parameters["id"])
                    val notification = notificationService.markAsRead(notifId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(notification))
                }

                put("read-all") {
                    val userId = userId(call)
                    notificationService.markAllAsRead(userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success("Todas las notificaciones marcadas como leídas"))
                }

                delete("{id}") {
                    val userId = userId(call)
                    val notifId = UUID.fromString(call.parameters["id"])
                    notificationService.delete(notifId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success("Notificación eliminada"))
                }
            }
        }
    }

    private fun userId(call: ApplicationCall): UUID {
        val principal = call.principal<JWTPrincipal>()
        return principal?.payload?.subject?.let { UUID.fromString(it) }
            ?: throw IllegalArgumentException("Usuario no autenticado")
    }
}
