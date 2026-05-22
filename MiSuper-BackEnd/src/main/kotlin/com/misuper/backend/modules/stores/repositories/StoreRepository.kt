package com.misuper.backend.modules.stores.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.StoresTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime
import java.util.UUID

class StoreRepository {

    private val db get() = DatabaseFactory.getDatabase()

    fun findById(id: UUID): ResultRow? = transaction(db) {
        StoresTable.selectAll().where { StoresTable.id eq id }.singleOrNull()
    }

    fun findAll(): List<ResultRow> = transaction(db) {
        StoresTable.selectAll().where { StoresTable.active eq true }.toList()
    }

    fun create(
        nameVal: String,
        addressVal: String?,
        phoneVal: String?,
        latitudeVal: Double?,
        longitudeVal: Double?
    ): UUID = transaction(db) {
        StoresTable.insert {
            it[name] = nameVal
            it[address] = addressVal
            it[phone] = phoneVal
            it[latitude] = latitudeVal
            it[longitude] = longitudeVal
        }[StoresTable.id].value
    }

    fun update(
        id: UUID,
        nameVal: String?,
        addressVal: String?,
        phoneVal: String?,
        latitudeVal: Double?,
        longitudeVal: Double?
    ) = transaction(db) {
        StoresTable.update({ StoresTable.id eq id }) { stmt ->
            nameVal?.let { stmt[name] = it }
            addressVal?.let { stmt[address] = it }
            phoneVal?.let { stmt[phone] = it }
            latitudeVal?.let { stmt[latitude] = it }
            longitudeVal?.let { stmt[longitude] = it }
            stmt[updatedAt] = LocalDateTime.now()
        }
    }

    fun softDelete(id: UUID) = transaction(db) {
        StoresTable.update({ StoresTable.id eq id }) { stmt ->
            stmt[active] = false
            stmt[updatedAt] = LocalDateTime.now()
        }
    }
}
