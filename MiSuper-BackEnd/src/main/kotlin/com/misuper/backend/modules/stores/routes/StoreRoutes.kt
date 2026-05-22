package com.misuper.backend.modules.stores.routes

import com.misuper.backend.exceptions.ForbiddenException
import com.misuper.backend.modules.stores.dto.CreateStoreRequest
import com.misuper.backend.modules.stores.dto.UpdateStoreRequest
import com.misuper.backend.modules.stores.services.StoreService
import com.misuper.backend.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

class StoreRoutes(private val storeService: StoreService) {

    fun register(routing: Route) {
        routing.route("stores") {

            authenticate("auth-jwt") {
                get {
                    val stores = storeService.getAll()
                    call.respond(HttpStatusCode.OK, ApiResponse.success(stores))
                }

                get("{id}") {
                    val id = UUID.fromString(call.parameters["id"])
                    val store = storeService.getById(id)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(store))
                }

                post {
                    requireAdminRole(call)
                    val request = call.receive<CreateStoreRequest>()
                    val store = storeService.create(request)
                    call.respond(HttpStatusCode.Created, ApiResponse.success(store))
                }

                put("{id}") {
                    requireAdminRole(call)
                    val id = UUID.fromString(call.parameters["id"])
                    val request = call.receive<UpdateStoreRequest>()
                    val store = storeService.update(id, request)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(store))
                }

                delete("{id}") {
                    requireAdminRole(call)
                    val id = UUID.fromString(call.parameters["id"])
                    storeService.delete(id)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(mapOf("message" to "Tienda eliminada")))
                }
            }
        }
    }

    private fun requireAdminRole(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.payload?.getClaim("role")?.asString()
        if (role != "ADMIN") {
            throw ForbiddenException("Se requiere rol de administrador")
        }
    }
}
