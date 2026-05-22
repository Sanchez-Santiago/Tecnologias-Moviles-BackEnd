package com.misuper.backend.plugins

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.modules.auth.routes.AuthRoutes
import com.misuper.backend.modules.budgets.routes.BudgetRoutes
import com.misuper.backend.modules.groups.routes.GroupRoutes
import com.misuper.backend.modules.notifications.routes.NotificationRoutes
import com.misuper.backend.modules.offers.routes.OfferRoutes
import com.misuper.backend.modules.statistics.routes.StatisticsRoutes
import com.misuper.backend.modules.tickets.routes.TicketRoutes
import com.misuper.backend.modules.products.routes.ProductRoutes
import com.misuper.backend.modules.purchases.routes.PurchaseRoutes
import com.misuper.backend.modules.stores.routes.StoreRoutes
import com.misuper.backend.modules.users.routes.UserRoutes
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

fun Application.configureRouting(
    authRoutes: AuthRoutes,
    userRoutes: UserRoutes,
    productRoutes: ProductRoutes,
    storeRoutes: StoreRoutes,
    groupRoutes: GroupRoutes,
    purchaseRoutes: PurchaseRoutes,
    budgetRoutes: BudgetRoutes,
    ticketRoutes: TicketRoutes,
    notificationRoutes: NotificationRoutes,
    statisticsRoutes: StatisticsRoutes,
    offerRoutes: OfferRoutes,
    serverPort: Int,
    startTime: Long
) {
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
            userRoutes.register(this)
            productRoutes.register(this)
            storeRoutes.register(this)
            groupRoutes.register(this)
            purchaseRoutes.register(this)
            budgetRoutes.register(this)
            ticketRoutes.register(this)
            notificationRoutes.register(this)
            statisticsRoutes.register(this)
            offerRoutes.register(this)
        }
    }
}
