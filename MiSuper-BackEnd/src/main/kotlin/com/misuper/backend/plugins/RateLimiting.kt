package com.misuper.backend.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.minutes

val AuthRateLimit = RateLimitName("auth")

fun Application.configureRateLimiting() {
    install(RateLimit) {
        register(AuthRateLimit) {
            rateLimiter(limit = 10, refillPeriod = 1.minutes)
        }
    }
}
