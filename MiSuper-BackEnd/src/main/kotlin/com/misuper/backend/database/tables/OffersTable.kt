package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

object OffersTable : UUIDTable("offers") {
    val storeId: Column<EntityID<UUID>?> = reference("store_id", StoresTable).nullable()
    val title: Column<String> = varchar("title", 200)
    val description: Column<String?> = text("description").nullable()
    val discountType: Column<String> = varchar("discount_type", 20)
    val discountValue: Column<BigDecimal> = decimal("discount_value", 12, 2)
    val startDate: Column<LocalDateTime> = datetime("start_date")
    val endDate: Column<LocalDateTime> = datetime("end_date")
    val imageUrl: Column<String?> = text("image_url").nullable()
    val termsConditions: Column<String?> = text("terms_conditions").nullable()
    val active: Column<Boolean> = bool("active").default(true)
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt: Column<LocalDateTime> = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
