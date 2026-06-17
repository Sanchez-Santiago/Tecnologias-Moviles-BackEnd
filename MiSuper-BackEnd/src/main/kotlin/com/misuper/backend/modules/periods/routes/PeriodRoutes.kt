package com.misuper.backend.modules.periods.routes

import com.misuper.backend.modules.periods.dto.ClosePeriodRequest
import com.misuper.backend.modules.periods.dto.CreatePeriodRequest
import com.misuper.backend.modules.periods.services.PeriodService
import com.misuper.backend.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

class PeriodRoutes(private val periodService: PeriodService) {

    fun register(routing: Route) {
        routing.route("groups/{groupId}/periods") {

            authenticate("auth-jwt") {
                get("current") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["groupId"])
                    val period = periodService.getCurrentPeriod(groupId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(period))
                }

                get {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["groupId"])
                    val periods = periodService.getPeriods(groupId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(periods))
                }

                get("{id}") {
                    val userId = userId(call)
                    val periodId = UUID.fromString(call.parameters["id"])
                    val period = periodService.getPeriodById(periodId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(period))
                }

                post {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["groupId"])
                    val request = call.receive<CreatePeriodRequest>()
                    val period = periodService.createPeriod(groupId, userId, request)
                    call.respond(HttpStatusCode.Created, ApiResponse.success(period))
                }

                post("{id}/close") {
                    val userId = userId(call)
                    val periodId = UUID.fromString(call.parameters["id"])
                    val request = call.receive<ClosePeriodRequest>()
                    val period = periodService.closePeriod(periodId, userId, request)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(period))
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
