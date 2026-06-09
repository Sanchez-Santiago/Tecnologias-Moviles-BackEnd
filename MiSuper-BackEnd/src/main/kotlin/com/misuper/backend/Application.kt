@file:JvmName("Application")

package com.misuper.backend

import com.misuper.backend.config.AppConfig
import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.modules.auth.repositories.AuthRepository
import com.misuper.backend.modules.auth.routes.AuthRoutes
import com.misuper.backend.modules.auth.services.AuthService
import com.misuper.backend.modules.groups.repositories.GroupInvitationRepository
import com.misuper.backend.modules.groups.repositories.GroupRepository
import com.misuper.backend.modules.groups.routes.GroupRoutes
import com.misuper.backend.modules.groups.services.GroupInvitationService
import com.misuper.backend.modules.groups.services.GroupService
import com.misuper.backend.modules.products.repositories.CategoryRepository
import com.misuper.backend.modules.products.repositories.ProductRepository
import com.misuper.backend.modules.products.routes.ProductRoutes
import com.misuper.backend.modules.products.services.CategoryService
import com.misuper.backend.modules.products.services.ProductService
import com.misuper.backend.modules.budgets.repositories.BudgetRepository
import com.misuper.backend.modules.budgets.routes.BudgetRoutes
import com.misuper.backend.modules.budgets.services.BudgetService
import com.misuper.backend.modules.notifications.repositories.NotificationRepository
import com.misuper.backend.modules.notifications.routes.NotificationRoutes
import com.misuper.backend.modules.notifications.services.NotificationService
import com.misuper.backend.modules.offers.repositories.OfferRepository
import com.misuper.backend.modules.offers.routes.OfferRoutes
import com.misuper.backend.modules.offers.services.OfferService
import com.misuper.backend.modules.purchases.repositories.PurchaseRepository
import com.misuper.backend.modules.statistics.routes.StatisticsRoutes
import com.misuper.backend.modules.statistics.services.StatisticsService
import com.misuper.backend.modules.tickets.repositories.TicketRepository
import com.misuper.backend.modules.tickets.routes.TicketRoutes
import com.misuper.backend.modules.tickets.services.TicketService
import com.misuper.backend.modules.transactions.repositories.FinancialTransactionRepository
import com.misuper.backend.modules.transactions.routes.FinancialTransactionRoutes
import com.misuper.backend.modules.transactions.services.FinancialTransactionService
import com.misuper.backend.modules.purchases.routes.PurchaseRoutes
import com.misuper.backend.modules.purchases.services.PurchaseService
import com.misuper.backend.modules.stores.repositories.StoreRepository
import com.misuper.backend.modules.stores.routes.StoreRoutes
import com.misuper.backend.modules.stores.services.StoreService
import com.misuper.backend.modules.users.repositories.UserRepository
import com.misuper.backend.modules.users.routes.UserRoutes
import com.misuper.backend.modules.users.services.UserService
import com.misuper.backend.plugins.*
import com.misuper.backend.security.JwtService
import com.misuper.backend.security.PasswordHasher
import com.misuper.backend.security.TokenConfig
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.File
import java.nio.file.Paths

private fun loadEnv() {
    val keys = listOf(
        "PORT",
        "HOST",
        "DATABASE_URL",
        "DATABASE_USER",
        "DATABASE_PASSWORD",
        "DATABASE_MAX_POOL_SIZE",
        "DATABASE_MIGRATE_ON_START",
        "DATABASE_SEED_ON_START",
        "JWT_SECRET",
        "JWT_ISSUER",
        "JWT_AUDIENCE",
        "ACCESS_TOKEN_EXPIRY_MINUTES",
        "REFRESH_TOKEN_EXPIRY_DAYS",
        "PASSWORD_HASH_COST",
        "PASSWORD_HISTORY_SIZE",
        "CORS_ALLOWED_HOSTS",
        "OPENAI_API_KEY",
        "OPENAI_MODEL"
    )

    val startDir = File(System.getProperty("user.dir") ?: ".")
    val searchDirs = mutableListOf(startDir)

    try {
        val classLocation = AppConfig::class.java.protectionDomain.codeSource?.location?.toURI()
        if (classLocation != null) {
            val classFile = File(classLocation)
            searchDirs.add(classFile)
            classFile.parentFile?.let { searchDirs.add(it) }
        }
    } catch (e: Exception) {
        // Ignore errors resolving code source location
    }

    val envFile = searchDirs
        .asSequence()
        .flatMap { dir -> generateSequence(dir) { it.parentFile } }
        .distinct()
        .flatMap { dir ->
            sequenceOf(
                File(dir, ".env"),
                File(dir, "MiSuper-BackEnd/.env"),
                File(dir, "backend/.env"),
                File(dir, "src/.env")
            )
        }
        .firstOrNull { it.exists() }

    if (envFile != null) {
        envFile.readLines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .forEach { line ->
                val idx = line.indexOf('=')
                if (idx > 0) {
                    System.setProperty(line.substring(0, idx).trim(), line.substring(idx + 1).trim())
                }
            }
        println("[INFO] Loaded env from ${envFile.absolutePath}")
    } else {
        println("[WARN] No .env file found (searched from ${startDir.absolutePath})")
    }

    keys.forEach { key ->
        if (System.getProperty(key) == null) {
            val envVal = System.getenv(key)
            if (envVal != null) {
                System.setProperty(key, envVal)
            }
        }
    }
}

fun main() {
    loadEnv()
    val startTime = System.currentTimeMillis()

    val appConfig = AppConfig.load()

    DatabaseFactory.init(appConfig.database)

    val tokenConfig = TokenConfig(
        secret = appConfig.jwt.secret,
        issuer = appConfig.jwt.issuer,
        audience = appConfig.jwt.audience,
        accessExpirationMinutes = appConfig.jwt.accessExpirationMinutes,
        refreshExpirationDays = appConfig.jwt.refreshExpirationDays
    )

    val jwtService = JwtService(tokenConfig)
    val passwordHasher = PasswordHasher(appConfig.password.hashCost)
    val authRepository = AuthRepository()
    val authService = AuthService(authRepository, jwtService, passwordHasher, appConfig.password.historySize)
    val authRoutes = AuthRoutes(authService)

    val userRepository = UserRepository()
    val userService = UserService(userRepository, passwordHasher, appConfig.password.historySize)
    val userRoutes = UserRoutes(userService)

    val categoryRepository = CategoryRepository()
    val productRepository = ProductRepository()
    val categoryService = CategoryService(categoryRepository)
    val productService = ProductService(productRepository, categoryRepository)
    val productRoutes = ProductRoutes(productService, categoryService)

    val storeRepository = StoreRepository()
    val storeService = StoreService(storeRepository)
    val storeRoutes = StoreRoutes(storeService)

    val groupRepository = GroupRepository()
    val groupService = GroupService(groupRepository, authRepository)
    val groupInvitationRepository = GroupInvitationRepository()
    val groupInvitationService = GroupInvitationService(groupInvitationRepository, groupRepository, authRepository)
    val groupRoutes = GroupRoutes(groupService, groupInvitationService)

    val purchaseRepository = PurchaseRepository()
    val purchaseService = PurchaseService(purchaseRepository, productRepository, storeRepository, groupRepository)
    val purchaseRoutes = PurchaseRoutes(purchaseService)

    val budgetRepository = BudgetRepository()
    val budgetService = BudgetService(budgetRepository, groupRepository)
    val budgetRoutes = BudgetRoutes(budgetService)

    val ticketRepository = TicketRepository()
    val ticketService = TicketService(ticketRepository, groupRepository)
    val ticketRoutes = TicketRoutes(ticketService)

    val notificationRepository = NotificationRepository()
    val notificationService = NotificationService(notificationRepository)
    val notificationRoutes = NotificationRoutes(notificationService)

    val statisticsService = StatisticsService(
        purchaseRepository = purchaseRepository,
        productRepository = productRepository,
        categoryRepository = categoryRepository,
        storeRepository = storeRepository,
        budgetRepository = budgetRepository,
        groupRepository = groupRepository
    )
    val statisticsRoutes = StatisticsRoutes(statisticsService)

    val offerRepository = OfferRepository()
    val offerService = OfferService(offerRepository, storeRepository, productRepository)
    val offerRoutes = OfferRoutes(offerService)

    val financialTransactionRepository = FinancialTransactionRepository()
    val financialTransactionService = FinancialTransactionService(financialTransactionRepository, groupRepository)
    val financialTransactionRoutes = FinancialTransactionRoutes(financialTransactionService)

    embeddedServer(Netty, port = appConfig.serverPort) {
        configureCors(appConfig.corsAllowedHosts)
        configureLogging()
        configureSerialization()
        configureStatusPages()
        configureRateLimiting()
        configureSecurity(jwtService)
        configureRouting(authRoutes, userRoutes, productRoutes, storeRoutes, groupRoutes, purchaseRoutes, budgetRoutes, ticketRoutes, notificationRoutes, statisticsRoutes, offerRoutes, financialTransactionRoutes, appConfig.serverPort, startTime)

        monitor.subscribe(ApplicationStarted) {
            val url = "http://localhost:${appConfig.serverPort}"
            val statusLine = "  Status : Running successfully"
            val urlLine = "  URL    : $url"
            val portLine = "  Port   : ${appConfig.serverPort}"

            println("╔══════════════════════════════════════════════════════╗")
            println("║              MiSuper Backend - Running               ║")
            println("╠══════════════════════════════════════════════════════╣")
            println("║${statusLine.padEnd(54)}║")
            println("║${urlLine.padEnd(54)}║")
            println("║${portLine.padEnd(54)}║")
            println("╚══════════════════════════════════════════════════════╝")
        }
    }.start(wait = true)
}
