package com.misuper.backend.modules.shoppinglist.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.GroupsTable
import com.misuper.backend.database.tables.ProductsTable
import com.misuper.backend.database.tables.ShoppingListItemsTable
import com.misuper.backend.database.tables.ShoppingListsTable
import com.misuper.backend.database.tables.UsersTable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import java.time.LocalDate
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

    fun findByGroupIdAndDateRange(groupIdVal: UUID, from: LocalDate?, to: LocalDate?): List<ResultRow> = transaction(db) {
        val groupIdEntity = EntityID(groupIdVal, GroupsTable)
        val base = ShoppingListsTable.groupId eq groupIdEntity
        val conditions = mutableListOf(base)
        from?.let { conditions.add(ShoppingListsTable.createdAt greaterEq it.atStartOfDay()) }
        to?.let { conditions.add(ShoppingListsTable.createdAt lessEq it.plusDays(1).atStartOfDay()) }
        val finalCondition = conditions.reduce { a, b -> a.and(b) }
        ShoppingListsTable.selectAll()
            .where { finalCondition }
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
        ShoppingListItemsTable.deleteWhere {
            shoppingListId eq EntityID(id, ShoppingListsTable)
        }
        ShoppingListsTable.deleteWhere { ShoppingListsTable.id eq id }
    }

    fun getProducts(shoppingListIdVal: UUID): List<ResultRow> = transaction(db) {
        ShoppingListItemsTable.selectAll()
            .where { ShoppingListItemsTable.shoppingListId eq EntityID(shoppingListIdVal, ShoppingListsTable) }
            .toList()
    }

    fun addProduct(
        shoppingListIdVal: UUID,
        productIdVal: UUID?,
        customProductNameVal: String?,
        estimatedPriceVal: BigDecimal?,
        estimatedQuantityVal: BigDecimal?,
        estimatedBrandVal: String?,
        unitVal: String?,
        priorityVal: String?,
        notesVal: String?
    ): UUID = transaction(db) {
        ShoppingListItemsTable.insert { stmt ->
            stmt[ShoppingListItemsTable.shoppingListId] = EntityID(shoppingListIdVal, ShoppingListsTable)
            if (productIdVal != null) {
                stmt[ShoppingListItemsTable.productId] = EntityID(productIdVal, ProductsTable)
            }
            stmt[ShoppingListItemsTable.customProductName] = customProductNameVal
            if (estimatedPriceVal != null) stmt[ShoppingListItemsTable.estimatedPrice] = estimatedPriceVal
            if (estimatedQuantityVal != null) stmt[ShoppingListItemsTable.estimatedQuantity] = estimatedQuantityVal
            stmt[ShoppingListItemsTable.estimatedBrand] = estimatedBrandVal
            stmt[ShoppingListItemsTable.unit] = unitVal
            if (priorityVal != null) stmt[ShoppingListItemsTable.priority] = priorityVal
            stmt[ShoppingListItemsTable.notes] = notesVal
        }[ShoppingListItemsTable.id].value
    }

    fun updateProduct(
        id: UUID,
        checkedVal: Boolean?,
        notesVal: String?
    ) = transaction(db) {
        ShoppingListItemsTable.update({ ShoppingListItemsTable.id eq id }) { stmt ->
            checkedVal?.let {
                stmt[ShoppingListItemsTable.checked] = it
                if (it) stmt[ShoppingListItemsTable.lastCheckedAt] = LocalDateTime.now()
            }
            if (notesVal != null) stmt[ShoppingListItemsTable.notes] = notesVal
            stmt[ShoppingListItemsTable.updatedAt] = LocalDateTime.now()
        }
    }

    fun deleteProduct(id: UUID) = transaction(db) {
        ShoppingListItemsTable.deleteWhere { ShoppingListItemsTable.id eq id }
    }

    fun findProductById(id: UUID): ResultRow? = transaction(db) {
        ProductsTable.selectAll().where { ProductsTable.id eq id }.singleOrNull()
    }

    fun findProductByName(nameVal: String): ResultRow? = transaction(db) {
        ProductsTable.selectAll().where { ProductsTable.name eq nameVal }.singleOrNull()
    }
}
