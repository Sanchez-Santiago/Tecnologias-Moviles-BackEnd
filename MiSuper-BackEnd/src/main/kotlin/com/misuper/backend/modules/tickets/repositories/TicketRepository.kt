package com.misuper.backend.modules.tickets.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

class TicketRepository {

    private val db get() = DatabaseFactory.getDatabase()

    fun findById(id: UUID): ResultRow? = transaction(db) {
        TicketsTable.selectAll().where { TicketsTable.id eq id }.singleOrNull()
    }

    fun findByGroupId(groupIdVal: UUID, limit: Int = 50): List<ResultRow> = transaction(db) {
        TicketsTable.selectAll()
            .where { TicketsTable.groupId eq EntityID(groupIdVal, GroupsTable) }
            .orderBy(TicketsTable.purchaseDate, SortOrder.DESC_NULLS_LAST)
            .limit(limit)
            .toList()
    }

    fun findByGroupIdAndDateRange(groupIdVal: UUID, from: LocalDate, to: LocalDate): List<ResultRow> = transaction(db) {
        TicketsTable.selectAll()
            .where {
                (TicketsTable.groupId eq EntityID(groupIdVal, GroupsTable)) and
                    (TicketsTable.purchaseDate greaterEq from.atStartOfDay()) and
                    (TicketsTable.purchaseDate lessEq to.atTime(LocalTime.MAX))
            }
            .orderBy(TicketsTable.purchaseDate, SortOrder.DESC_NULLS_LAST)
            .toList()
    }

    fun findExpensesByGroupId(groupIdVal: UUID): List<ResultRow> = transaction(db) {
        TicketsTable.selectAll()
            .where {
                (TicketsTable.groupId eq EntityID(groupIdVal, GroupsTable)) and
                    (TicketsTable.movementType eq "EXPENSE")
            }
            .orderBy(TicketsTable.purchaseDate, SortOrder.DESC_NULLS_LAST)
            .toList()
    }

    fun findExpensesByGroupIdAndDateRange(groupIdVal: UUID, from: LocalDate, to: LocalDate): List<ResultRow> = transaction(db) {
        TicketsTable.selectAll()
            .where {
                (TicketsTable.groupId eq EntityID(groupIdVal, GroupsTable)) and
                    (TicketsTable.movementType eq "EXPENSE") and
                    (TicketsTable.purchaseDate greaterEq from.atStartOfDay()) and
                    (TicketsTable.purchaseDate lessEq to.atTime(LocalTime.MAX))
            }
            .orderBy(TicketsTable.purchaseDate, SortOrder.DESC_NULLS_LAST)
            .toList()
    }

    fun create(
        groupIdVal: UUID,
        uploadedByVal: UUID,
        supermarketNameVal: String,
        amountVal: BigDecimal,
        movementTypeVal: String?,
        purchaseDateVal: LocalDateTime,
        commentVal: String?,
        imageUrlVal: String?,
        statusVal: String = "PENDING"
    ): UUID = transaction(db) {
        TicketsTable.insert { stmt ->
            stmt[TicketsTable.groupId] = EntityID(groupIdVal, GroupsTable)
            stmt[TicketsTable.uploadedBy] = EntityID(uploadedByVal, UsersTable)
            stmt[TicketsTable.supermarketName] = supermarketNameVal
            stmt[TicketsTable.amount] = amountVal
            stmt[TicketsTable.movementType] = movementTypeVal
            stmt[TicketsTable.purchaseDate] = purchaseDateVal
            stmt[TicketsTable.comment] = commentVal
            stmt[TicketsTable.imageUrl] = imageUrlVal
            stmt[TicketsTable.status] = statusVal
        }[TicketsTable.id].value
    }

    fun update(
        id: UUID,
        supermarketNameVal: String?,
        commentVal: String?,
        imageUrlVal: String?
    ) = transaction(db) {
        TicketsTable.update({ TicketsTable.id eq id }) { stmt ->
            supermarketNameVal?.let { stmt[TicketsTable.supermarketName] = it }
            commentVal?.let { stmt[TicketsTable.comment] = it }
            imageUrlVal?.let { stmt[TicketsTable.imageUrl] = it }
            stmt[TicketsTable.updatedAt] = LocalDateTime.now()
        }
    }

    fun delete(id: UUID) = transaction(db) {
        TicketProductsTable.deleteWhere { ticketId eq EntityID(id, TicketsTable) }
        TicketsTable.deleteWhere { TicketsTable.id eq id }
    }

    fun getProducts(ticketIdVal: UUID): List<ResultRow> = transaction(db) {
        TicketProductsTable.selectAll()
            .where { TicketProductsTable.ticketId eq EntityID(ticketIdVal, TicketsTable) }
            .toList()
    }

    fun addProduct(
        ticketIdVal: UUID,
        productIdVal: UUID?,
        detectedNameVal: String?,
        quantityVal: BigDecimal?,
        priceVal: BigDecimal?,
        brandVal: String?
    ): UUID = transaction(db) {
        TicketProductsTable.insert { stmt ->
            stmt[TicketProductsTable.ticketId] = EntityID(ticketIdVal, TicketsTable)
            stmt[TicketProductsTable.detectedName] = detectedNameVal
            if (productIdVal != null) {
                stmt[TicketProductsTable.productId] = EntityID(productIdVal, ProductsTable)
            }
            stmt[TicketProductsTable.quantity] = quantityVal
            stmt[TicketProductsTable.price] = priceVal
            stmt[TicketProductsTable.brand] = brandVal
        }[TicketProductsTable.id].value
    }
}
