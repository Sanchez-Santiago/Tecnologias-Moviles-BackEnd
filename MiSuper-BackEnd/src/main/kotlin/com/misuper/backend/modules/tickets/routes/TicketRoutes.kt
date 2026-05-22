package com.misuper.backend.modules.tickets.routes

import com.misuper.backend.modules.tickets.dto.AddMessageRequest
import com.misuper.backend.modules.tickets.dto.CreateTicketRequest
import com.misuper.backend.modules.tickets.dto.UpdateTicketRequest
import com.misuper.backend.modules.tickets.services.TicketService
import com.misuper.backend.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

class TicketRoutes(private val ticketService: TicketService) {

    fun register(routing: Route) {
        routing.route("tickets") {

            authenticate("auth-jwt") {
                get {
                    val userId = userId(call)
                    val groupIdStr = call.request.queryParameters["groupId"]
                        ?: throw IllegalArgumentException("El parámetro groupId es obligatorio")
                    val groupId = UUID.fromString(groupIdStr)
                    val tickets = ticketService.getByGroup(groupId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(tickets))
                }

                get("{id}") {
                    val userId = userId(call)
                    val ticketId = UUID.fromString(call.parameters["id"])
                    val ticket = ticketService.getById(ticketId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(ticket))
                }

                post {
                    val userId = userId(call)
                    val request = call.receive<CreateTicketRequest>()
                    val ticket = ticketService.create(userId, request)
                    call.respond(HttpStatusCode.Created, ApiResponse.success(ticket))
                }

                put("{id}") {
                    val userId = userId(call)
                    val ticketId = UUID.fromString(call.parameters["id"])
                    val request = call.receive<UpdateTicketRequest>()
                    val ticket = ticketService.update(ticketId, userId, request)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(ticket))
                }

                delete("{id}") {
                    val userId = userId(call)
                    val ticketId = UUID.fromString(call.parameters["id"])
                    ticketService.softDelete(ticketId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success("Ticket eliminado"))
                }

                post("{id}/messages") {
                    val userId = userId(call)
                    val ticketId = UUID.fromString(call.parameters["id"])
                    val request = call.receive<AddMessageRequest>()
                    val message = ticketService.addMessage(ticketId, userId, request)
                    call.respond(HttpStatusCode.Created, ApiResponse.success(message))
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
