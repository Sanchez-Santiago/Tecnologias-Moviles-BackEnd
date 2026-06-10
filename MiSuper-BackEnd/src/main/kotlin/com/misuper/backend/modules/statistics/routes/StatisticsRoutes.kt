package com.misuper.backend.modules.statistics.routes

import com.misuper.backend.modules.statistics.services.StatisticsService
import com.misuper.backend.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

class StatisticsRoutes(private val statisticsService: StatisticsService) {

    fun register(routing: Route) {
        routing.route("statistics") {

            authenticate("auth-jwt") {
                get("group/{groupId}/spending-by-category") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["groupId"])
                    val stats = statisticsService.getSpendingByCategory(groupId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(stats))
                }

                get("group/{groupId}/spending-by-importance") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["groupId"])
                    val stats = statisticsService.getSpendingByImportance(groupId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(stats))
                }

                get("group/{groupId}/spending-by-store") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["groupId"])
                    val stats = statisticsService.getSpendingByStore(groupId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(stats))
                }

                get("group/{groupId}/monthly-summary") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["groupId"])
                    val stats = statisticsService.getMonthlySummary(groupId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(stats))
                }

                get("group/{groupId}/most-frequent-store") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["groupId"])
                    val stats = statisticsService.getMostFrequentStore(groupId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(stats))
                }

                get("group/{groupId}/budget-progress") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["groupId"])
                    val stats = statisticsService.getBudgetProgress(groupId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(stats))
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
