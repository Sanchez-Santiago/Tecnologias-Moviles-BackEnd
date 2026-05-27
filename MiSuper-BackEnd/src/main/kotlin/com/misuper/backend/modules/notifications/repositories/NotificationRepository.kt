package com.misuper.backend.modules.notifications.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.NotificationsTable
import com.misuper.backend.database.tables.UsersTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class NotificationRepository {

    private val db get() = DatabaseFactory.getDatabase()

    fun findByUserId(userIdVal: UUID): List<ResultRow> = transaction(db) {
        NotificationsTable.selectAll()
            .where {
                (NotificationsTable.userId eq EntityID(userIdVal, UsersTable)) and
                    (NotificationsTable.active eq true)
            }
            .orderBy(NotificationsTable.createdAt, SortOrder.DESC_NULLS_LAST)
            .toList()
    }

    fun findByIdAndUser(id: UUID, userIdVal: UUID): ResultRow? = transaction(db) {
        NotificationsTable.selectAll()
            .where {
                (NotificationsTable.id eq id) and
                    (NotificationsTable.userId eq EntityID(userIdVal, UsersTable)) and
                    (NotificationsTable.active eq true)
            }
            .singleOrNull()
    }

    fun create(
        userIdVal: UUID,
        typeVal: String,
        titleVal: String,
        messageVal: String,
        dataVal: String?
    ) = transaction(db) {
        NotificationsTable.insert { stmt ->
            stmt[NotificationsTable.userId] = EntityID(userIdVal, UsersTable)
            stmt[NotificationsTable.type] = typeVal
            stmt[NotificationsTable.title] = titleVal
            stmt[NotificationsTable.message] = messageVal
            stmt[NotificationsTable.data] = dataVal
        }
    }

    fun markAsRead(id: UUID) = transaction(db) {
        NotificationsTable.update({ NotificationsTable.id eq id }) { stmt ->
            stmt[NotificationsTable.read] = true
        }
    }

    fun markAllAsRead(userIdVal: UUID) = transaction(db) {
        NotificationsTable.update({
            (NotificationsTable.userId eq EntityID(userIdVal, UsersTable)) and
                (NotificationsTable.active eq true)
        }) { stmt ->
            stmt[NotificationsTable.read] = true
        }
    }

    fun softDelete(id: UUID) = transaction(db) {
        NotificationsTable.update({ NotificationsTable.id eq id }) { stmt ->
            stmt[NotificationsTable.active] = false
        }
    }

    fun countUnread(userIdVal: UUID): Int = transaction(db) {
        NotificationsTable.selectAll()
            .where {
                (NotificationsTable.userId eq EntityID(userIdVal, UsersTable)) and
                    (NotificationsTable.active eq true) and
                    (NotificationsTable.read eq false)
            }
            .count().toInt()
    }
}
