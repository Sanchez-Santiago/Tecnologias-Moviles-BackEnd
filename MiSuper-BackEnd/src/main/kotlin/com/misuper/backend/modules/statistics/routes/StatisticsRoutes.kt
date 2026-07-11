package com.misuper.backend.modules.statistics.routes

import com.misuper.backend.modules.statistics.services.StatisticsService
import com.misuper.backend.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate
import java.util.UUID

class StatisticsRoutes(private val statisticsService: StatisticsService) {

    fun register(routing: Route) {
        routing.route("statistics") {

            authenticate("auth-jwt") {
                get("group/{groupId}/spending-by-category") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["groupId"])
                    val (from, to) = parseDateParams(call)
                    val stats = statisticsService.getSpendingByCategory(groupId, userId, from, to)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(stats))
                }

                get("group/{groupId}/spending-by-importance") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["groupId"])
                    val (from, to) = parseDateParams(call)
                    val stats = statisticsService.getSpendingByImportance(groupId, userId, from, to)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(stats))
                }

                get("group/{groupId}/spending-by-store") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["groupId"])
                    val (from, to) = parseDateParams(call)
                    val stats = statisticsService.getSpendingByStore(groupId, userId, from, to)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(stats))
                }

                get("group/{groupId}/monthly-summary") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["groupId"])
                    val (from, to) = parseDateParams(call)
                    val stats = statisticsService.getMonthlySummary(groupId, userId, from, to)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(stats))
                }

                get("group/{groupId}/most-frequent-store") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["groupId"])
                    val (from, to) = parseDateParams(call)
                    val stats = statisticsService.getMostFrequentStore(groupId, userId, from, to)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(stats))
                }

                get("group/{groupId}/budget-progress") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["groupId"])
                    val (from, to) = parseDateParams(call)
                    val stats = statisticsService.getBudgetProgress(groupId, userId, from, to)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(stats))
                }

                get("group/{groupId}/most-purchased-products") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["groupId"])
                    val (from, to) = parseDateParams(call)
                    val stats = statisticsService.getMostPurchasedProducts(groupId, userId, from, to)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(stats))
                }

                get("group/{groupId}/member-spending") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["groupId"])
                    val (from, to) = parseDateParams(call)
                    val stats = statisticsService.getMemberSpending(groupId, userId, from, to)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(stats))
                }
            }
        }
    }

    private fun parseDateParams(call: ApplicationCall): Pair<LocalDate?, LocalDate?> {
        val from = call.request.queryParameters["from"]?.let { if (it.isNotBlank()) LocalDate.parse(it) else null }
        val to = call.request.queryParameters["to"]?.let { if (it.isNotBlank()) LocalDate.parse(it) else null }
        return from to to
    }

    private fun userId(call: ApplicationCall): UUID {
        val principal = call.principal<JWTPrincipal>()
        return principal?.payload?.subject?.let { UUID.fromString(it) }
            ?: throw IllegalArgumentException("Usuario no autenticado")
    }
}
