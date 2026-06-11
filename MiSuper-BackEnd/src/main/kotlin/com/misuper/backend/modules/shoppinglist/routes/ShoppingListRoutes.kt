package com.misuper.backend.modules.shoppinglist.routes

import com.misuper.backend.modules.shoppinglist.dto.AddProductRequest
import com.misuper.backend.modules.shoppinglist.dto.CreateShoppingListRequest
import com.misuper.backend.modules.shoppinglist.dto.UpdateProductRequest
import com.misuper.backend.modules.shoppinglist.dto.UpdateShoppingListRequest
import com.misuper.backend.modules.shoppinglist.services.ShoppingListService
import com.misuper.backend.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

class ShoppingListRoutes(private val shoppingListService: ShoppingListService) {

    fun register(routing: Route) {
        routing.route("shopping-lists") {

            authenticate("auth-jwt") {
                get {
                    val userId = userId(call)
                    val groupIdStr = call.request.queryParameters["groupId"]
                        ?: throw IllegalArgumentException("El parámetro groupId es obligatorio")
                    val groupId = UUID.fromString(groupIdStr)
                    val lists = shoppingListService.getByGroup(groupId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(lists))
                }

                get("{id}") {
                    val userId = userId(call)
                    val listId = UUID.fromString(call.parameters["id"])
                    val list = shoppingListService.getById(listId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(list))
                }

                post {
                    val userId = userId(call)
                    val request = call.receive<CreateShoppingListRequest>()
                    val list = shoppingListService.create(userId, request)
                    call.respond(HttpStatusCode.Created, ApiResponse.success(list))
                }

                put("{id}") {
                    val userId = userId(call)
                    val listId = UUID.fromString(call.parameters["id"])
                    val request = call.receive<UpdateShoppingListRequest>()
                    val list = shoppingListService.update(listId, userId, request)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(list))
                }

                delete("{id}") {
                    val userId = userId(call)
                    val listId = UUID.fromString(call.parameters["id"])
                    shoppingListService.delete(listId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success("Lista eliminada"))
                }

                post("{id}/products") {
                    val userId = userId(call)
                    val listId = UUID.fromString(call.parameters["id"])
                    val request = call.receive<AddProductRequest>()
                    val list = shoppingListService.addProduct(listId, userId, request)
                    call.respond(HttpStatusCode.Created, ApiResponse.success(list))
                }

                put("{id}/products/{productId}") {
                    val userId = userId(call)
                    val listId = UUID.fromString(call.parameters["id"])
                    val productUuid = UUID.fromString(call.parameters["productId"])
                    val request = call.receive<UpdateProductRequest>()
                    val list = shoppingListService.updateProduct(listId, productUuid, userId, request)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(list))
                }

                delete("{id}/products/{productId}") {
                    val userId = userId(call)
                    val listId = UUID.fromString(call.parameters["id"])
                    val productUuid = UUID.fromString(call.parameters["productId"])
                    val list = shoppingListService.deleteProduct(listId, productUuid, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(list))
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
