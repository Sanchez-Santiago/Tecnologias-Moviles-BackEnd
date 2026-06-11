package com.misuper.backend.modules.shoppinglist.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.GroupsTable
import com.misuper.backend.database.tables.ProductsTable
import com.misuper.backend.database.tables.ShoppingListProductsTable
import com.misuper.backend.database.tables.ShoppingListsTable
import com.misuper.backend.database.tables.UsersTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class ShoppingListRepository {

    private val db get() = DatabaseFactory.getDatabase()

    fun findById(id: UUID): ResultRow? = transaction(db) {
        ShoppingListsTable.selectAll().where { ShoppingListsTable.id eq id }.singleOrNull()
    }

    fun findByGroupId(groupIdVal: UUID): List<ResultRow> = transaction(db) {
        ShoppingListsTable.selectAll()
            .where { ShoppingListsTable.groupId eq EntityID(groupIdVal, GroupsTable) }
            .orderBy(ShoppingListsTable.createdAt, SortOrder.DESC_NULLS_LAST)
            .toList()
    }

    fun create(
        groupIdVal: UUID,
        createdByVal: UUID?,
        nameVal: String,
        descriptionVal: String?
    ): UUID = transaction(db) {
        ShoppingListsTable.insert { stmt ->
            stmt[ShoppingListsTable.groupId] = EntityID(groupIdVal, GroupsTable)
            if (createdByVal != null) {
                stmt[ShoppingListsTable.createdBy] = EntityID(createdByVal, UsersTable)
            }
            stmt[ShoppingListsTable.name] = nameVal
            stmt[ShoppingListsTable.description] = descriptionVal
        }[ShoppingListsTable.id].value
    }

    fun update(id: UUID, nameVal: String?, descriptionVal: String?) = transaction(db) {
        ShoppingListsTable.update({ ShoppingListsTable.id eq id }) { stmt ->
            nameVal?.let { stmt[ShoppingListsTable.name] = it }
            if (descriptionVal != null) stmt[ShoppingListsTable.description] = descriptionVal
        }
    }

    fun delete(id: UUID) = transaction(db) {
        ShoppingListProductsTable.deleteWhere {
            shoppingListId eq EntityID(id, ShoppingListsTable)
        }
        ShoppingListsTable.deleteWhere { ShoppingListsTable.id eq id }
    }

    fun getProducts(shoppingListIdVal: UUID): List<ResultRow> = transaction(db) {
        ShoppingListProductsTable.selectAll()
            .where { ShoppingListProductsTable.shoppingListId eq EntityID(shoppingListIdVal, ShoppingListsTable) }
            .toList()
    }

    fun addProduct(
        shoppingListIdVal: UUID,
        productIdVal: UUID,
        quantityVal: BigDecimal?,
        notesVal: String?
    ): UUID = transaction(db) {
        ShoppingListProductsTable.insert { stmt ->
            stmt[ShoppingListProductsTable.shoppingListId] = EntityID(shoppingListIdVal, ShoppingListsTable)
            stmt[ShoppingListProductsTable.productId] = EntityID(productIdVal, ProductsTable)
            if (quantityVal != null) stmt[ShoppingListProductsTable.finalQuantity] = quantityVal
            stmt[ShoppingListProductsTable.notes] = notesVal
        }[ShoppingListProductsTable.id].value
    }

    fun updateProduct(
        id: UUID,
        checkedVal: Boolean?,
        finalPriceVal: BigDecimal?,
        finalQuantityVal: BigDecimal?,
        notesVal: String?
    ) = transaction(db) {
        ShoppingListProductsTable.update({ ShoppingListProductsTable.id eq id }) { stmt ->
            checkedVal?.let { stmt[ShoppingListProductsTable.checked] = it }
            if (finalPriceVal != null) stmt[ShoppingListProductsTable.finalPrice] = finalPriceVal
            if (finalQuantityVal != null) stmt[ShoppingListProductsTable.finalQuantity] = finalQuantityVal
            if (notesVal != null) stmt[ShoppingListProductsTable.notes] = notesVal
        }
    }

    fun deleteProduct(id: UUID) = transaction(db) {
        ShoppingListProductsTable.deleteWhere { ShoppingListProductsTable.id eq id }
    }

    fun findProductById(id: UUID): ResultRow? = transaction(db) {
        ProductsTable.selectAll().where { ProductsTable.id eq id }.singleOrNull()
    }

    fun findProductByName(nameVal: String): ResultRow? = transaction(db) {
        ProductsTable.selectAll().where { ProductsTable.name eq nameVal }.singleOrNull()
    }
}
