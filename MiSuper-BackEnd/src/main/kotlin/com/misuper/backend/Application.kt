@file:JvmName("Application")

package com.misuper.backend

import com.misuper.backend.config.AppConfig
import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.modules.auth.repositories.AuthRepository
import com.misuper.backend.modules.auth.routes.AuthRoutes
import com.misuper.backend.modules.auth.services.AuthService
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
    val keys = listOf("DATABASE_URL", "DATABASE_USER", "DATABASE_PASSWORD", "JWT_SECRET")

    val startDir = File(System.getProperty("user.dir") ?: ".")

    val envFile = generateSequence(startDir) { it.parentFile }
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
    val authService = AuthService(authRepository, jwtService, passwordHasher)
    val authRoutes = AuthRoutes(authService)

    embeddedServer(Netty, port = appConfig.serverPort) {
        configureSerialization()
        configureStatusPages()
        configureSecurity(jwtService)
        configureRouting(authRoutes, appConfig.serverPort, startTime)

        monitor.subscribe(ApplicationStarted) {
            val url = "http://localhost:${appConfig.serverPort}"
            println("╔══════════════════════════════════════════════════════╗")
            println("║              MiSuper Backend - Running              ║")
            println("╠══════════════════════════════════════════════════════╣")
            println("║  Status : Running successfully                      ║")
            println("║  URL    : ${url.padEnd(48)}║")
            println("║  Port   : ${appConfig.serverPort.toString().padEnd(48)}║")
            println("╚══════════════════════════════════════════════════════╝")
        }
    }.start(wait = true)
}
