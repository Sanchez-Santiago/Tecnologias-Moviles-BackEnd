package com.misuper.backend.plugins

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.modules.auth.routes.AuthRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class HealthResponse(
    val status: String,
    val server: ServerInfo,
    val database: DatabaseInfo,
    val uptime: Long,
    val timestamp: String = Instant.now().toString()
)

@Serializable
data class ServerInfo(
    val port: Int,
    val host: String = "localhost"
)

@Serializable
data class DatabaseInfo(
    val connected: Boolean,
    val activeConnections: Int,
    val idleConnections: Int
)

fun Application.configureRouting(authRoutes: AuthRoutes, serverPort: Int, startTime: Long) {
    routing {
        get("/") {
            val uptime = System.currentTimeMillis() - startTime
            call.respond(
                HealthResponse(
                    status = "ok",
                    server = ServerInfo(port = serverPort),
                    database = DatabaseInfo(
                        connected = DatabaseFactory.isConnected(),
                        activeConnections = DatabaseFactory.getActiveConnections(),
                        idleConnections = DatabaseFactory.getIdleConnections()
                    ),
                    uptime = uptime / 1000
                )
            )
        }
        route("api") {
            authRoutes.register(this)
        }
    }
}
