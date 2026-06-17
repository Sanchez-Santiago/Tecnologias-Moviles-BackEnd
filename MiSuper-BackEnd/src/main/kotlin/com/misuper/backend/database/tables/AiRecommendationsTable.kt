package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

object AiRecommendationsTable : UUIDTable("ai_recommendations") {
    val groupId: Column<EntityID<UUID>> = reference("group_id", GroupsTable)
    val periodId: Column<EntityID<UUID>> = reference("period_id", GroupMonthlyPeriodsTable)
    val title: Column<String> = varchar("title", 255)
    val description: Column<String?> = text("description").nullable()
    val type: Column<String> = varchar("type", 50)
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
}
