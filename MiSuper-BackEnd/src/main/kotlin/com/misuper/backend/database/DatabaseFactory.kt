package com.misuper.backend.database

import com.misuper.backend.config.DatabaseConfig
import com.misuper.backend.database.tables.LoginHistoryTable
import com.misuper.backend.database.tables.PasswordHistoryTable
import com.misuper.backend.database.tables.RefreshTokensTable
import com.misuper.backend.database.tables.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import javax.sql.DataSource

object DatabaseFactory {
    private var dataSource: DataSource? = null
    private var database: Database? = null

    fun init(config: DatabaseConfig) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.url
            username = config.user
            password = config.password
            driverClassName = config.driver
            maximumPoolSize = config.maxPoolSize
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            addDataSourceProperty("prepareThreshold", "0")
            validate()
        }

        val hikariDS = HikariDataSource(hikariConfig)
        dataSource = hikariDS

        database = Database.connect(hikariDS)

        transaction(database!!) {
            SchemaUtils.create(UsersTable, PasswordHistoryTable, RefreshTokensTable, LoginHistoryTable)
        }
    }

    fun getDatabase(): Database = database ?: throw IllegalStateException("Database not initialized")

    fun isConnected(): Boolean {
        val hikariDS = dataSource as? HikariDataSource ?: return false
        return !hikariDS.isClosed && hikariDS.isRunning
    }

    fun getActiveConnections(): Int {
        return (dataSource as? HikariDataSource)?.hikariPoolMXBean?.activeConnections ?: 0
    }

    fun getIdleConnections(): Int {
        return (dataSource as? HikariDataSource)?.hikariPoolMXBean?.idleConnections ?: 0
    }

    fun close() {
        (dataSource as? HikariDataSource)?.close()
    }
}
