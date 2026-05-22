package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

object PurchasesTable : UUIDTable("purchases") {
    val groupId: Column<EntityID<UUID>> = reference("group_id", GroupsTable)
    val storeId: Column<EntityID<UUID>?> = reference("store_id", StoresTable).nullable()
    val userId: Column<EntityID<UUID>> = reference("user_id", UsersTable)
    val total: Column<BigDecimal> = decimal("total", 12, 2)
    val notes: Column<String?> = text("notes").nullable()
    val purchaseDate: Column<LocalDateTime> = datetime("purchase_date").clientDefault { LocalDateTime.now() }
    val active: Column<Boolean> = bool("active").default(true)
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt: Column<LocalDateTime> = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
