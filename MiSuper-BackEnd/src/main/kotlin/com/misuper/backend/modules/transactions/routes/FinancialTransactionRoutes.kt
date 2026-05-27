package com.misuper.backend.modules.transactions.routes

import com.misuper.backend.modules.transactions.dto.CreateFinancialTransactionRequest
import com.misuper.backend.modules.transactions.services.FinancialTransactionService
import com.misuper.backend.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

class FinancialTransactionRoutes(private val service: FinancialTransactionService) {
    fun register(routing: Route) {
        routing.route("transactions") {
            authenticate("auth-jwt") {
                get {
                    val groupId = UUID.fromString(
                        call.request.queryParameters["groupId"]
                            ?: throw IllegalArgumentException("El parámetro groupId es obligatorio")
                    )
                    call.respond(HttpStatusCode.OK, ApiResponse.success(service.getByGroup(groupId, userId(call))))
                }

                get("summary") {
                    val groupId = UUID.fromString(
                        call.request.queryParameters["groupId"]
                            ?: throw IllegalArgumentException("El parámetro groupId es obligatorio")
                    )
                    call.respond(HttpStatusCode.OK, ApiResponse.success(service.summary(groupId, userId(call))))
                }

                post {
                    val request = call.receive<CreateFinancialTransactionRequest>()
                    call.respond(HttpStatusCode.Created, ApiResponse.success(service.create(userId(call), request)))
                }

                delete("{id}") {
                    service.delete(UUID.fromString(call.parameters["id"]), userId(call))
                    call.respond(HttpStatusCode.OK, ApiResponse.success(mapOf("message" to "Movimiento eliminado")))
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
