package com.misuper.backend.modules.budgets.routes

import com.misuper.backend.modules.budgets.dto.CreateBudgetRequest
import com.misuper.backend.modules.budgets.dto.UpdateBudgetRequest
import com.misuper.backend.modules.budgets.services.BudgetService
import com.misuper.backend.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

class BudgetRoutes(private val budgetService: BudgetService) {

    fun register(routing: Route) {
        routing.route("budgets") {

            authenticate("auth-jwt") {
                get {
                    val userId = userId(call)
                    val groupIdStr = call.request.queryParameters["groupId"]
                        ?: throw IllegalArgumentException("El parámetro groupId es obligatorio")
                    val groupId = UUID.fromString(groupIdStr)
                    val budgets = budgetService.getByGroup(groupId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(budgets))
                }

                get("{id}") {
                    val userId = userId(call)
                    val budgetId = UUID.fromString(call.parameters["id"])
                    val budget = budgetService.getById(budgetId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(budget))
                }

                post {
                    val userId = userId(call)
                    val request = call.receive<CreateBudgetRequest>()
                    val budget = budgetService.create(userId, request)
                    call.respond(HttpStatusCode.Created, ApiResponse.success(budget))
                }

                put("{id}") {
                    val userId = userId(call)
                    val budgetId = UUID.fromString(call.parameters["id"])
                    val request = call.receive<UpdateBudgetRequest>()
                    val budget = budgetService.update(budgetId, userId, request)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(budget))
                }

                patch("{id}/activate") {
                    val userId = userId(call)
                    val budgetId = UUID.fromString(call.parameters["id"])
                    val budget = budgetService.activate(budgetId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(budget))
                }

                delete("{id}") {
                    val userId = userId(call)
                    val budgetId = UUID.fromString(call.parameters["id"])
                    budgetService.softDelete(budgetId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success("Presupuesto eliminado"))
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
