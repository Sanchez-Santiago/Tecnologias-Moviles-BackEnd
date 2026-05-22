package com.misuper.backend.database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

object StoresTable : UUIDTable("stores") {
    val name: Column<String> = varchar("name", 255)
    val address: Column<String?> = text("address").nullable()
    val phone: Column<String?> = varchar("phone", 50).nullable()
    val latitude: Column<Double?> = double("latitude").nullable()
    val longitude: Column<Double?> = double("longitude").nullable()
    val active: Column<Boolean> = bool("active").default(true)
    val createdAt: Column<LocalDateTime> = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt: Column<LocalDateTime> = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
