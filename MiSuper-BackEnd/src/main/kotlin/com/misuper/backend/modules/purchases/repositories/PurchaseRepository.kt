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
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.time.LocalDateTime
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

    fun createWithItems(
        groupIdVal: UUID,
        storeIdVal: UUID?,
        userIdVal: UUID,
        totalVal: BigDecimal,
        notesVal: String?,
        items: List<PurchaseItemInsert>
    ): UUID = transaction(db) {
        val purchaseId = PurchasesTable.insert { stmt ->
            stmt[PurchasesTable.groupId] = EntityID(groupIdVal, GroupsTable)
            if (storeIdVal != null) {
                stmt[PurchasesTable.storeId] = EntityID(storeIdVal, StoresTable)
            }
            stmt[PurchasesTable.userId] = EntityID(userIdVal, UsersTable)
            stmt[PurchasesTable.total] = totalVal
            stmt[PurchasesTable.notes] = notesVal
        }[PurchasesTable.id].value

        items.forEach { item ->
            PurchaseProductsTable.insert { stmt ->
                stmt[PurchaseProductsTable.purchaseId] = EntityID(purchaseId, PurchasesTable)
                stmt[PurchaseProductsTable.productId] = EntityID(item.productId, ProductsTable)
                stmt[PurchaseProductsTable.productName] = item.productName
                stmt[PurchaseProductsTable.quantity] = item.quantity
                stmt[PurchaseProductsTable.unitPrice] = item.unitPrice
                stmt[PurchaseProductsTable.subtotal] = item.subtotal
            }
        }

        purchaseId
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

    fun update(id: UUID, storeIdVal: UUID?, notesVal: String?) = transaction(db) {
        PurchasesTable.update({ PurchasesTable.id eq id }) { stmt ->
            storeIdVal?.let { sid -> stmt[PurchasesTable.storeId] = EntityID(sid, StoresTable) }
            if (notesVal != null) stmt[PurchasesTable.notes] = notesVal
            stmt[PurchasesTable.updatedAt] = LocalDateTime.now()
        }
    }

    fun findUserIdsByProductIds(productIds: List<UUID>): List<UUID> = transaction(db) {
        if (productIds.isEmpty()) return@transaction emptyList()

        val conditions = productIds.map {
            PurchaseProductsTable.productId eq EntityID(it, ProductsTable)
        }
        val combined = conditions.reduce { a, b -> a or b }

        val purchaseIds = PurchaseProductsTable.selectAll()
            .where(combined)
            .map { it[PurchaseProductsTable.purchaseId].value }
            .distinct()

        if (purchaseIds.isEmpty()) return@transaction emptyList<UUID>()

        val purchaseConditions = purchaseIds.map {
            PurchasesTable.id eq EntityID(it, PurchasesTable)
        }
        val purchaseCombined = purchaseConditions.reduce { a, b -> a or b }

        PurchasesTable.selectAll()
            .where(purchaseCombined)
            .map { it[PurchasesTable.userId].value }
            .distinct()
    }

    fun softDelete(id: UUID) = transaction(db) {
        PurchasesTable.update({ PurchasesTable.id eq id }) { stmt ->
            stmt[PurchasesTable.active] = false
            stmt[PurchasesTable.updatedAt] = LocalDateTime.now()
        }
    }
}

data class PurchaseItemInsert(
    val productId: UUID,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal
)
