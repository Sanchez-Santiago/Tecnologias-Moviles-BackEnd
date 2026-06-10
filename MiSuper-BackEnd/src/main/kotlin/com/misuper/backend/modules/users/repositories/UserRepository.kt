package com.misuper.backend.modules.users.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.PasswordHistoryTable
import com.misuper.backend.database.tables.UserSettingsTable
import com.misuper.backend.database.tables.UsersTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime
import java.util.UUID

class UserRepository {

    private val db get() = DatabaseFactory.getDatabase()

    fun findById(id: UUID): ResultRow? = transaction(db) {
        UsersTable.selectAll().where { UsersTable.id eq id }.singleOrNull()
    }

    fun findByEmail(email: String): ResultRow? = transaction(db) {
        UsersTable.selectAll().where { UsersTable.email eq email }.singleOrNull()
    }

    fun updateProfile(
        userId: UUID,
        fullNameVal: String,
        emailVal: String?,
        phoneVal: String?,
        alternativePhoneVal: String?,
        profilePictureUrlVal: String?
    ) = transaction(db) {
        UsersTable.update({ UsersTable.id eq userId }) {
            it[fullName] = fullNameVal
            emailVal?.let { e -> it[email] = e }
            it[phone] = phoneVal
            it[alternativePhone] = alternativePhoneVal
            it[profilePictureUrl] = profilePictureUrlVal
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun updatePassword(userId: UUID, newPasswordHash: String) = transaction(db) {
        UsersTable.update({ UsersTable.id eq userId }) {
            it[passwordHash] = newPasswordHash
            it[updatedAt] = LocalDateTime.now()
        }
        PasswordHistoryTable.insert {
            it[this.userId] = EntityID(userId, UsersTable)
            it[passwordHash] = newPasswordHash
            it[active] = true
            it[createdAt] = LocalDateTime.now()
        }
    }

    fun getPasswordHistory(userId: UUID, limit: Int): List<String> = transaction(db) {
        PasswordHistoryTable.selectAll()
            .where { PasswordHistoryTable.userId eq EntityID(userId, UsersTable) }
            .orderBy(PasswordHistoryTable.createdAt, org.jetbrains.exposed.v1.core.SortOrder.DESC)
            .limit(limit)
            .map { it[PasswordHistoryTable.passwordHash] }
    }

    fun getPasswordHash(userId: UUID): String? = transaction(db) {
        UsersTable.selectAll()
            .where { UsersTable.id eq userId }
            .singleOrNull()
            ?.get(UsersTable.passwordHash)
    }

    fun getSettings(userId: UUID): ResultRow? = transaction(db) {
        UserSettingsTable.selectAll()
            .where { UserSettingsTable.userId eq EntityID(userId, UsersTable) }
            .singleOrNull()
    }

    fun initSettings(userId: UUID) = transaction(db) {
        val exists = UserSettingsTable.selectAll()
            .where { UserSettingsTable.userId eq EntityID(userId, UsersTable) }
            .singleOrNull()
        if (exists == null) {
            UserSettingsTable.insert {
                it[this.userId] = EntityID(userId, UsersTable)
                it[language] = "es"
                it[notificationsEnabled] = true
                it[currency] = "ARS"
            }
        }
    }

    fun updateSettings(
        userId: UUID,
        languageVal: String?,
        notificationsEnabledVal: Boolean?,
        currencyVal: String?
    ) = transaction(db) {
        UserSettingsTable.update({ UserSettingsTable.userId eq EntityID(userId, UsersTable) }) { stmt ->
            languageVal?.let { lang -> stmt[UserSettingsTable.language] = lang }
            notificationsEnabledVal?.let { enabled -> stmt[UserSettingsTable.notificationsEnabled] = enabled }
            currencyVal?.let { cur -> stmt[UserSettingsTable.currency] = cur }
            stmt[UserSettingsTable.updatedAt] = LocalDateTime.now()
        }
    }
}
