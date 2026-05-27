package com.misuper.backend.modules.products.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.CategoriesTable
import com.misuper.backend.database.tables.ProductsTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class ProductRepository {

    private val db get() = DatabaseFactory.getDatabase()

    fun findById(id: UUID): ResultRow? = transaction(db) {
        ProductsTable
            .selectAll()
            .where { ProductsTable.id eq id }
            .singleOrNull()
    }

    fun findAll(categoryId: UUID? = null): List<ResultRow> = transaction(db) {
        val query = if (categoryId != null) {
            ProductsTable.selectAll().where {
                (ProductsTable.active eq true) and
                    (ProductsTable.categoryId eq EntityID(categoryId, CategoriesTable))
            }
        } else {
            ProductsTable.selectAll().where { ProductsTable.active eq true }
        }
        query.toList()
    }

    fun create(
        nameVal: String,
        priceVal: BigDecimal,
        categoryIdVal: UUID,
        descriptionVal: String?,
        imageUrlVal: String?,
        barcodeVal: String?,
        priorityVal: String
    ): UUID = transaction(db) {
        ProductsTable.insert {
            it[name] = nameVal
            it[price] = priceVal
            it[categoryId] = EntityID(categoryIdVal, CategoriesTable)
            it[description] = descriptionVal
            it[imageUrl] = imageUrlVal
            it[barcode] = barcodeVal
            it[priority] = priorityVal
        }[ProductsTable.id].value
    }

    fun update(
        id: UUID,
        nameVal: String?,
        priceVal: BigDecimal?,
        categoryIdVal: UUID?,
        descriptionVal: String?,
        imageUrlVal: String?,
        barcodeVal: String?,
        priorityVal: String?
    ) = transaction(db) {
        ProductsTable.update({ ProductsTable.id eq id }) { stmt ->
            nameVal?.let { stmt[name] = it }
            priceVal?.let { stmt[price] = it }
            categoryIdVal?.let { stmt[categoryId] = EntityID(it, CategoriesTable) }
            descriptionVal?.let { stmt[description] = it }
            imageUrlVal?.let { stmt[imageUrl] = it }
            barcodeVal?.let { stmt[barcode] = it }
            priorityVal?.let { stmt[priority] = it }
            stmt[updatedAt] = LocalDateTime.now()
        }
    }

    fun softDelete(id: UUID) = transaction(db) {
        ProductsTable.update({ ProductsTable.id eq id }) { stmt ->
            stmt[active] = false
            stmt[updatedAt] = LocalDateTime.now()
        }
    }
}
