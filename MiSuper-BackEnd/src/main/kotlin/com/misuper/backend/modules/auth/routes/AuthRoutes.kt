package com.misuper.backend.modules.auth.routes

import com.misuper.backend.modules.auth.dto.LoginRequest
import com.misuper.backend.modules.auth.dto.RefreshRequest
import com.misuper.backend.modules.auth.dto.RegisterRequest
import com.misuper.backend.modules.auth.services.AuthService
import com.misuper.backend.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.request.host
import io.ktor.server.response.*
import io.ktor.server.routing.*

class AuthRoutes(private val authService: AuthService) {

    fun register(routing: Route) {
        routing.route("auth") {
            post("register") {
                val request = call.receive<RegisterRequest>()
                val ip = call.request.host()
                val userAgent = call.request.headers["User-Agent"]
                val response = authService.register(request, ip, userAgent)
                call.respond(HttpStatusCode.Created, ApiResponse.success(response))
            }

            post("login") {
                val request = call.receive<LoginRequest>()
                val ip = call.request.host()
                val userAgent = call.request.headers["User-Agent"]
                val response = authService.login(request, ip, userAgent)
                call.respond(HttpStatusCode.OK, ApiResponse.success(response))
            }

            post("refresh") {
                val request = call.receive<RefreshRequest>()
                val response = authService.refreshToken(request.refreshToken)
                call.respond(HttpStatusCode.OK, ApiResponse.success(response))
            }
        }
    }
}
