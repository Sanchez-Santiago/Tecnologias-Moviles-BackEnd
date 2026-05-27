package com.misuper.backend.modules.offers.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.OffersTable
import com.misuper.backend.database.tables.StoresTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class OfferRepository {

    private val db get() = DatabaseFactory.getDatabase()

    fun findById(id: UUID): ResultRow? = transaction(db) {
        OffersTable.selectAll().where { OffersTable.id eq id }.singleOrNull()
    }

    fun findAll(storeIdVal: UUID? = null): List<ResultRow> = transaction(db) {
        val all = OffersTable.selectAll()
            .where { OffersTable.active eq true }
            .orderBy(OffersTable.createdAt, SortOrder.DESC_NULLS_LAST)
            .toList()

        if (storeIdVal != null) {
            all.filter { row ->
                val sid = row[OffersTable.storeId]
                sid == null || sid.value == storeIdVal
            }
        } else {
            all
        }
    }

    fun findActiveAt(moment: LocalDateTime, storeIdVal: UUID? = null): List<ResultRow> = transaction(db) {
        val all = OffersTable.selectAll()
            .where {
                (OffersTable.active eq true) and
                    (OffersTable.startDate lessEq moment) and
                    (OffersTable.endDate greaterEq moment)
            }
            .orderBy(OffersTable.createdAt, SortOrder.DESC_NULLS_LAST)
            .toList()

        if (storeIdVal != null) {
            all.filter { row ->
                val sid = row[OffersTable.storeId]
                sid == null || sid.value == storeIdVal
            }
        } else {
            all
        }
    }

    fun create(
        storeIdVal: UUID?,
        titleVal: String,
        descriptionVal: String?,
        discountTypeVal: String,
        discountValueVal: BigDecimal,
        startDateVal: LocalDateTime,
        endDateVal: LocalDateTime,
        imageUrlVal: String?,
        termsConditionsVal: String?
    ): UUID = transaction(db) {
        OffersTable.insert { stmt ->
            if (storeIdVal != null) {
                stmt[OffersTable.storeId] = EntityID(storeIdVal, StoresTable)
            }
            stmt[OffersTable.title] = titleVal
            stmt[OffersTable.description] = descriptionVal
            stmt[OffersTable.discountType] = discountTypeVal
            stmt[OffersTable.discountValue] = discountValueVal
            stmt[OffersTable.startDate] = startDateVal
            stmt[OffersTable.endDate] = endDateVal
            stmt[OffersTable.imageUrl] = imageUrlVal
            stmt[OffersTable.termsConditions] = termsConditionsVal
        }[OffersTable.id].value
    }

    fun update(
        id: UUID,
        storeIdVal: UUID?,
        titleVal: String?,
        descriptionVal: String?,
        discountTypeVal: String?,
        discountValueVal: BigDecimal?,
        startDateVal: LocalDateTime?,
        endDateVal: LocalDateTime?,
        imageUrlVal: String?,
        termsConditionsVal: String?
    ) = transaction(db) {
        OffersTable.update({ OffersTable.id eq id }) { stmt ->
            if (storeIdVal != null) stmt[OffersTable.storeId] = EntityID(storeIdVal, StoresTable)
            titleVal?.let { stmt[OffersTable.title] = it }
            descriptionVal?.let { stmt[OffersTable.description] = it }
            discountTypeVal?.let { stmt[OffersTable.discountType] = it }
            discountValueVal?.let { stmt[OffersTable.discountValue] = it }
            startDateVal?.let { stmt[OffersTable.startDate] = it }
            endDateVal?.let { stmt[OffersTable.endDate] = it }
            imageUrlVal?.let { stmt[OffersTable.imageUrl] = it }
            termsConditionsVal?.let { stmt[OffersTable.termsConditions] = it }
            stmt[OffersTable.updatedAt] = LocalDateTime.now()
        }
    }

    fun softDelete(id: UUID) = transaction(db) {
        OffersTable.update({ OffersTable.id eq id }) { stmt ->
            stmt[OffersTable.active] = false
            stmt[OffersTable.updatedAt] = LocalDateTime.now()
        }
    }
}
