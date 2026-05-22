package com.misuper.backend.modules.products.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.CategoriesTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime
import java.util.UUID

class CategoryRepository {

    private val db get() = DatabaseFactory.getDatabase()

    fun findById(id: UUID): ResultRow? = transaction(db) {
        CategoriesTable.selectAll().where { CategoriesTable.id eq id }.singleOrNull()
    }

    fun findAll(): List<ResultRow> = transaction(db) {
        CategoriesTable.selectAll().where { CategoriesTable.active eq true }
            .toList()
    }

    fun create(nameVal: String, descriptionVal: String?, iconVal: String?): UUID = transaction(db) {
        CategoriesTable.insert {
            it[name] = nameVal
            it[description] = descriptionVal
            it[icon] = iconVal
        }[CategoriesTable.id].value
    }

    fun update(id: UUID, nameVal: String?, descriptionVal: String?, iconVal: String?) = transaction(db) {
        CategoriesTable.update({ CategoriesTable.id eq id }) { stmt ->
            nameVal?.let { stmt[name] = it }
            descriptionVal?.let { stmt[description] = it }
            iconVal?.let { stmt[icon] = it }
            stmt[updatedAt] = LocalDateTime.now()
        }
    }

    fun softDelete(id: UUID) = transaction(db) {
        CategoriesTable.update({ CategoriesTable.id eq id }) { stmt ->
            stmt[active] = false
            stmt[updatedAt] = LocalDateTime.now()
        }
    }
}
