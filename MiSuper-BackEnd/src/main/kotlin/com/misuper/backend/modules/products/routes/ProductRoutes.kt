package com.misuper.backend.modules.products.routes

import com.misuper.backend.modules.products.dto.CreateCategoryRequest
import com.misuper.backend.modules.products.dto.CreateProductRequest
import com.misuper.backend.modules.products.dto.UpdateProductRequest
import com.misuper.backend.modules.products.services.CategoryService
import com.misuper.backend.modules.products.services.ProductService
import com.misuper.backend.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

class ProductRoutes(
    private val productService: ProductService,
    private val categoryService: CategoryService
) {
    fun register(routing: Route) {
        routing.route("products") {

            authenticate("auth-jwt") {
                get {
                    val categoryIdStr = call.request.queryParameters["categoryId"]
                    val categoryId = categoryIdStr?.let { UUID.fromString(it) }
                    val products = productService.getAll(categoryId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(products))
                }

                get("{id}") {
                    val id = UUID.fromString(call.parameters["id"])
                    val product = productService.getById(id)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(product))
                }

                post {
                    requireAdminRole(call)
                    val request = call.receive<CreateProductRequest>()
                    val product = productService.create(request)
                    call.respond(HttpStatusCode.Created, ApiResponse.success(product))
                }

                put("{id}") {
                    requireAdminRole(call)
                    val id = UUID.fromString(call.parameters["id"])
                    val request = call.receive<UpdateProductRequest>()
                    val product = productService.update(id, request)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(product))
                }

                delete("{id}") {
                    requireAdminRole(call)
                    val id = UUID.fromString(call.parameters["id"])
                    productService.delete(id)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(mapOf("message" to "Producto eliminado")))
                }
            }
        }

        routing.route("categories") {

            authenticate("auth-jwt") {
                get {
                    val categories = categoryService.getAll()
                    call.respond(HttpStatusCode.OK, ApiResponse.success(categories))
                }

                get("{id}") {
                    val id = UUID.fromString(call.parameters["id"])
                    val category = categoryService.getById(id)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(category))
                }

                post {
                    requireAdminRole(call)
                    val request = call.receive<CreateCategoryRequest>()
                    val category = categoryService.create(request)
                    call.respond(HttpStatusCode.Created, ApiResponse.success(category))
                }

                put("{id}") {
                    requireAdminRole(call)
                    val id = UUID.fromString(call.parameters["id"])
                    val request = call.receive<CreateCategoryRequest>()
                    val category = categoryService.update(id, request)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(category))
                }

                delete("{id}") {
                    requireAdminRole(call)
                    val id = UUID.fromString(call.parameters["id"])
                    categoryService.delete(id)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(mapOf("message" to "Categoría eliminada")))
                }
            }
        }
    }

    private fun requireAdminRole(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.payload?.getClaim("role")?.asString()
        if (role != "ADMIN") {
            throw com.misuper.backend.exceptions.ForbiddenException("Se requiere rol de administrador")
        }
    }
}
