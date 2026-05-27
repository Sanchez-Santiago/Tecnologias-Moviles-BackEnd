package com.misuper.backend.modules.purchases.routes

import com.misuper.backend.modules.purchases.dto.CreatePurchaseRequest
import com.misuper.backend.modules.purchases.services.PurchaseService
import com.misuper.backend.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

class PurchaseRoutes(private val purchaseService: PurchaseService) {

    fun register(routing: Route) {
        routing.route("purchases") {

            authenticate("auth-jwt") {
                get {
                    val userId = userId(call)
                    val groupIdStr = call.request.queryParameters["groupId"]
                        ?: throw IllegalArgumentException("El parámetro groupId es obligatorio")
                    val groupId = UUID.fromString(groupIdStr)
                    val purchases = purchaseService.getByGroup(groupId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(purchases))
                }

                get("{id}") {
                    val userId = userId(call)
                    val purchaseId = UUID.fromString(call.parameters["id"])
                    val purchase = purchaseService.getById(purchaseId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(purchase))
                }

                get("{id}/share") {
                    val userId = userId(call)
                    val purchaseId = UUID.fromString(call.parameters["id"])
                    val share = purchaseService.getShareText(purchaseId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(share))
                }

                post {
                    val userId = userId(call)
                    val request = call.receive<CreatePurchaseRequest>()
                    val purchase = purchaseService.create(userId, request)
                    call.respond(HttpStatusCode.Created, ApiResponse.success(purchase))
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
