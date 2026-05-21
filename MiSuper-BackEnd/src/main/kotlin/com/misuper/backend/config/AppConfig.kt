package com.misuper.backend.config

import com.typesafe.config.ConfigFactory

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val driver: String,
    val maxPoolSize: Int
)

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val accessExpirationMinutes: Long,
    val refreshExpirationDays: Long
)

data class PasswordConfig(
    val hashCost: Int,
    val historySize: Int
)

data class AppConfig(
    val serverPort: Int,
    val database: DatabaseConfig,
    val jwt: JwtConfig,
    val password: PasswordConfig
) {
    companion object {
        fun load(): AppConfig {
            val config = ConfigFactory.load().resolve()

            val serverPort = config.getInt("server.port")
            val db = config.getConfig("database")
            val jwt = config.getConfig("jwt")
            val pwd = config.getConfig("password")

            return AppConfig(
                serverPort = serverPort,
                database = DatabaseConfig(
                    url = db.getString("url"),
                    user = db.getString("user"),
                    password = db.getString("password"),
                    driver = db.getString("driver"),
                    maxPoolSize = db.getInt("maxPoolSize")
                ),
                jwt = JwtConfig(
                    secret = jwt.getString("secret"),
                    issuer = jwt.getString("issuer"),
                    audience = jwt.getString("audience"),
                    accessExpirationMinutes = jwt.getLong("accessExpirationMinutes"),
                    refreshExpirationDays = jwt.getLong("refreshExpirationDays")
                ),
                password = PasswordConfig(
                    hashCost = pwd.getInt("hashCost"),
                    historySize = pwd.getInt("historySize")
                )
            )
        }
    }
}
