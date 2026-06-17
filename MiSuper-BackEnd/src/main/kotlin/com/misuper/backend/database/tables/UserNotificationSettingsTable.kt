package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

object UserNotificationSettingsTable : UUIDTable("user_notification_settings") {
    val userId: Column<EntityID<UUID>> = reference("user_id", UsersTable).uniqueIndex()
    val budgetNotifications: Column<Boolean> = bool("budget_notifications").default(true)
    val purchaseNotifications: Column<Boolean> = bool("purchase_notifications").default(true)
    val promotionNotifications: Column<Boolean> = bool("promotion_notifications").default(true)
    val securityNotifications: Column<Boolean> = bool("security_notifications").default(true)
    val groupNotifications: Column<Boolean> = bool("group_notifications").default(true)
    val aiNotifications: Column<Boolean> = bool("ai_notifications").default(true)
    val pushEnabled: Column<Boolean> = bool("push_enabled").default(true)
    val emailEnabled: Column<Boolean> = bool("email_enabled").default(false)
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
}
