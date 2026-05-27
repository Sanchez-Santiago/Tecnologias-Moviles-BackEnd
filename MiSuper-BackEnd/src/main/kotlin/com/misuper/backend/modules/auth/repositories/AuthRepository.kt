package com.misuper.backend.modules.auth.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDateTime
import java.util.UUID

class AuthRepository {

    private val db get() = DatabaseFactory.getDatabase()

    fun findByEmail(email: String): ResultRow? = transaction(db) {
        UsersTable.selectAll().where { UsersTable.email eq email }.singleOrNull()
    }

    fun findById(id: UUID): ResultRow? = transaction(db) {
        UsersTable.selectAll().where { UsersTable.id eq id }.singleOrNull()
    }

    fun createUser(fullNameVal: String, emailVal: String, passwordHashVal: String): UUID = transaction(db) {
        val insert = UsersTable.insert {
            it[fullName] = fullNameVal
            it[email] = emailVal
            it[role] = "USER"
            it[verified] = false
            it[failedAttempts] = 0
            it[blocked] = false
            it[passwordHash] = passwordHashVal
        }
        val userId = insert[UsersTable.id].value
        createPasswordHistory(userId, passwordHashVal)
        userId
    }

    private fun createPasswordHistory(userIdVal: UUID, passwordHashVal: String) = transaction(db) {
        PasswordHistoryTable.insert {
            it[userId] = EntityID(userIdVal, UsersTable)
            it[this.passwordHash] = passwordHashVal
            it[active] = true
        }
    }

    fun getPasswordHistory(userIdVal: UUID, limit: Int): List<String> = transaction(db) {
        PasswordHistoryTable.selectAll()
            .where { (PasswordHistoryTable.userId eq EntityID(userIdVal, UsersTable)) and (PasswordHistoryTable.active eq true) }
            .orderBy(PasswordHistoryTable.createdAt, org.jetbrains.exposed.v1.core.SortOrder.DESC)
            .limit(limit)
            .map { it[PasswordHistoryTable.passwordHash] }
    }

    fun getPasswordHash(userIdVal: UUID): String? = transaction(db) {
        UsersTable.selectAll()
            .where { UsersTable.id eq userIdVal }
            .singleOrNull()
            ?.get(UsersTable.passwordHash)
    }

    fun incrementFailedAttempts(userIdVal: UUID) = transaction(db) {
        val current = UsersTable.selectAll().where { UsersTable.id eq userIdVal }
            .singleOrNull()?.get(UsersTable.failedAttempts) ?: 0
        val newAttempts = current + 1
        UsersTable.update({ UsersTable.id eq userIdVal }) {
            it[this.failedAttempts] = newAttempts
            if (newAttempts >= 5) {
                it[this.blocked] = true
            }
            it[this.updatedAt] = LocalDateTime.now()
        }
    }

    fun resetFailedAttempts(userIdVal: UUID) = transaction(db) {
        UsersTable.update({ UsersTable.id eq userIdVal }) {
            it[this.failedAttempts] = 0
            it[this.updatedAt] = LocalDateTime.now()
        }
    }

    fun saveRefreshToken(userIdVal: UUID, tokenVal: String, expiresAtVal: LocalDateTime) = transaction(db) {
        RefreshTokensTable.insert {
            it[userId] = EntityID(userIdVal, UsersTable)
            it[token] = tokenVal
            it[expiresAt] = expiresAtVal
            it[used] = false
        }
    }

    fun findRefreshToken(tokenVal: String): ResultRow? = transaction(db) {
        RefreshTokensTable.selectAll()
            .where { (RefreshTokensTable.token eq tokenVal) and (RefreshTokensTable.used eq false) }
            .singleOrNull()
    }

    fun markRefreshTokenAsUsed(tokenIdVal: UUID) = transaction(db) {
        RefreshTokensTable.update({ RefreshTokensTable.id eq tokenIdVal }) {
            it[used] = true
        }
    }

    fun logLoginAttempt(userIdVal: UUID, ipAddressVal: String?, userAgentVal: String?, successVal: Boolean) = transaction(db) {
        LoginHistoryTable.insert {
            it[userId] = EntityID(userIdVal, UsersTable)
            it[ipAddress] = ipAddressVal
            it[userAgent] = userAgentVal
            it[success] = successVal
        }
    }
}
