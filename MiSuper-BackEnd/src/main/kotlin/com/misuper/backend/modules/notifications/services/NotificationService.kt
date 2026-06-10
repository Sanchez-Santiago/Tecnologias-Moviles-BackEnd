package com.misuper.backend.modules.notifications.services

import com.misuper.backend.database.tables.NotificationsTable
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.modules.notifications.dto.NotificationResponse
import com.misuper.backend.modules.notifications.repositories.NotificationRepository
import org.jetbrains.exposed.v1.core.ResultRow
import java.util.UUID

class NotificationService(
    private val notificationRepository: NotificationRepository
) {
    fun getByUser(userId: UUID): List<NotificationResponse> {
        return notificationRepository.findByUserId(userId).map { row ->
            buildResponse(row)
        }
    }

    fun markAsRead(id: UUID, userId: UUID): NotificationResponse {
        val row = notificationRepository.findByIdAndUser(id, userId)
            ?: throw NotFoundException("Notificación no encontrada")

        notificationRepository.markAsRead(id)

        val updated = notificationRepository.findByIdAndUser(id, userId)!!
        return buildResponse(updated)
    }

    fun markAllAsRead(userId: UUID) {
        notificationRepository.markAllAsRead(userId)
    }

    fun delete(id: UUID, userId: UUID) {
        val row = notificationRepository.findByIdAndUser(id, userId)
            ?: throw NotFoundException("Notificación no encontrada")

        notificationRepository.softDelete(id)
    }

    fun getUnreadCount(userId: UUID): Int {
        return notificationRepository.countUnread(userId)
    }

    fun create(userId: UUID, type: String, title: String, message: String, data: String? = null) {
        notificationRepository.create(userId, type, title, message, data)
    }

    private fun buildResponse(row: ResultRow): NotificationResponse {
        return NotificationResponse(
            id = row[NotificationsTable.id].value.toString(),
            type = row[NotificationsTable.type],
            title = row[NotificationsTable.title],
            message = row[NotificationsTable.message],
            data = row[NotificationsTable.data],
            read = row[NotificationsTable.read],
            createdAt = row[NotificationsTable.createdAt]
        )
    }
}
