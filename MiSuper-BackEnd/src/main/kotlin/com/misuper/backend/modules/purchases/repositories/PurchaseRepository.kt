package com.misuper.backend.modules.purchases.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.GroupsTable
import com.misuper.backend.database.tables.ProductsTable
import com.misuper.backend.database.tables.PurchaseProductsTable
import com.misuper.backend.database.tables.PurchasesTable
import com.misuper.backend.database.tables.StoresTable
import com.misuper.backend.database.tables.UsersTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.math.BigDecimal
import java.util.UUID

class PurchaseRepository {

    private val db get() = DatabaseFactory.getDatabase()

    fun findById(id: UUID): ResultRow? = transaction(db) {
        PurchasesTable.selectAll().where { PurchasesTable.id eq id }.singleOrNull()
    }

    fun findByGroupId(groupIdVal: UUID): List<ResultRow> = transaction(db) {
        PurchasesTable.selectAll()
            .where {
                (PurchasesTable.groupId eq EntityID(groupIdVal, GroupsTable)) and
                    (PurchasesTable.active eq true)
            }
            .orderBy(PurchasesTable.createdAt, SortOrder.DESC_NULLS_LAST)
            .toList()
    }

    fun create(
        groupIdVal: UUID,
        storeIdVal: UUID?,
        userIdVal: UUID,
        totalVal: BigDecimal,
        notesVal: String?
    ): UUID = transaction(db) {
        PurchasesTable.insert { stmt ->
            stmt[PurchasesTable.groupId] = EntityID(groupIdVal, GroupsTable)
            if (storeIdVal != null) {
                stmt[PurchasesTable.storeId] = EntityID(storeIdVal, StoresTable)
            }
            stmt[PurchasesTable.userId] = EntityID(userIdVal, UsersTable)
            stmt[PurchasesTable.total] = totalVal
            stmt[PurchasesTable.notes] = notesVal
        }[PurchasesTable.id].value
    }

    fun getItems(purchaseIdVal: UUID): List<ResultRow> = transaction(db) {
        PurchaseProductsTable.selectAll()
            .where { PurchaseProductsTable.purchaseId eq EntityID(purchaseIdVal, PurchasesTable) }
            .toList()
    }

    fun addItem(
        purchaseIdVal: UUID,
        productIdVal: UUID,
        productNameVal: String,
        quantityVal: Int,
        unitPriceVal: BigDecimal,
        subtotalVal: BigDecimal
    ) = transaction(db) {
        PurchaseProductsTable.insert { stmt ->
            stmt[PurchaseProductsTable.purchaseId] = EntityID(purchaseIdVal, PurchasesTable)
            stmt[PurchaseProductsTable.productId] = EntityID(productIdVal, ProductsTable)
            stmt[PurchaseProductsTable.productName] = productNameVal
            stmt[PurchaseProductsTable.quantity] = quantityVal
            stmt[PurchaseProductsTable.unitPrice] = unitPriceVal
            stmt[PurchaseProductsTable.subtotal] = subtotalVal
        }
    }
}
