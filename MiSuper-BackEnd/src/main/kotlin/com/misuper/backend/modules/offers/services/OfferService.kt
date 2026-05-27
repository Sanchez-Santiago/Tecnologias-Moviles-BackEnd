package com.misuper.backend.modules.offers.services

import com.misuper.backend.database.tables.OffersTable
import com.misuper.backend.database.tables.ProductsTable
import com.misuper.backend.database.tables.StoresTable
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.modules.offers.dto.CreateOfferRequest
import com.misuper.backend.modules.offers.dto.MatchedOfferResponse
import com.misuper.backend.modules.offers.dto.OfferResponse
import com.misuper.backend.modules.offers.dto.UpdateOfferRequest
import com.misuper.backend.modules.offers.repositories.OfferRepository
import com.misuper.backend.modules.offers.validators.OfferValidator
import com.misuper.backend.modules.products.repositories.ProductRepository
import com.misuper.backend.modules.stores.repositories.StoreRepository
import org.jetbrains.exposed.v1.core.ResultRow
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class OfferService(
    private val offerRepository: OfferRepository,
    private val storeRepository: StoreRepository,
    private val productRepository: ProductRepository
) {
    fun getAll(storeId: String? = null): List<OfferResponse> {
        val storeIdVal = storeId?.let { UUID.fromString(it) }
        return offerRepository.findAll(storeIdVal).map { row -> buildResponse(row) }
    }

    fun getById(id: UUID): OfferResponse {
        val row = offerRepository.findById(id)
            ?: throw NotFoundException("Oferta no encontrada")
        return buildResponse(row)
    }

    fun getActive(storeId: String? = null): List<OfferResponse> {
        val storeIdVal = storeId?.let { UUID.fromString(it) }
        return offerRepository.findActiveAt(LocalDateTime.now(), storeIdVal).map { row -> buildResponse(row) }
    }

    fun matchProducts(productIds: List<String>, storeId: String? = null): List<MatchedOfferResponse> {
        val ids = productIds.map { UUID.fromString(it) }
        val storeIdVal = storeId?.let { UUID.fromString(it) }
        val offers = offerRepository.findActiveAt(LocalDateTime.now(), storeIdVal)

        return ids.flatMap { productId ->
            val product = productRepository.findById(productId)
                ?: throw NotFoundException("Producto no encontrado: $productId")
            val name = product[ProductsTable.name]
            val normalizedName = name.lowercase()

            offers.mapNotNull { offer ->
                val haystack = listOfNotNull(
                    offer[OffersTable.title],
                    offer[OffersTable.description],
                    offer[OffersTable.termsConditions]
                ).joinToString(" ").lowercase()

                val isDirectMatch = normalizedName.split(" ")
                    .filter { it.length >= 4 }
                    .any { word -> haystack.contains(word) }

                val isGlobalOffer = offer[OffersTable.storeId] == null
                if (isDirectMatch || isGlobalOffer) {
                    MatchedOfferResponse(
                        productId = productId.toString(),
                        productName = name,
                        offer = buildResponse(offer),
                        reason = if (isDirectMatch) "Coincide con el producto" else "Oferta general activa"
                    )
                } else {
                    null
                }
            }
        }
    }

    fun create(request: CreateOfferRequest): OfferResponse {
        OfferValidator.validateCreate(request)

        val storeIdVal = request.storeId?.let { UUID.fromString(it) }
        storeIdVal?.let { sid ->
            storeRepository.findById(sid) ?: throw NotFoundException("Tienda no encontrada")
        }

        val startDate = LocalDateTime.parse(request.startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val endDate = LocalDateTime.parse(request.endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        val offerId = offerRepository.create(
            storeIdVal = storeIdVal,
            titleVal = request.title,
            descriptionVal = request.description,
            discountTypeVal = request.discountType.uppercase(),
            discountValueVal = BigDecimal.valueOf(request.discountValue),
            startDateVal = startDate,
            endDateVal = endDate,
            imageUrlVal = request.imageUrl,
            termsConditionsVal = request.termsConditions
        )

        return getById(offerId)
    }

    fun update(id: UUID, request: UpdateOfferRequest): OfferResponse {
        OfferValidator.validateUpdate(request)

        val row = offerRepository.findById(id)
            ?: throw NotFoundException("Oferta no encontrada")

        val storeIdVal = request.storeId?.let { UUID.fromString(it) }
        storeIdVal?.let { sid ->
            storeRepository.findById(sid) ?: throw NotFoundException("Tienda no encontrada")
        }

        val startDate = request.startDate?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
        val endDate = request.endDate?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }

        offerRepository.update(
            id = id,
            storeIdVal = storeIdVal,
            titleVal = request.title,
            descriptionVal = request.description,
            discountTypeVal = request.discountType?.uppercase(),
            discountValueVal = request.discountValue?.let { BigDecimal.valueOf(it) },
            startDateVal = startDate,
            endDateVal = endDate,
            imageUrlVal = request.imageUrl,
            termsConditionsVal = request.termsConditions
        )

        return getById(id)
    }

    fun softDelete(id: UUID) {
        val row = offerRepository.findById(id)
            ?: throw NotFoundException("Oferta no encontrada")
        offerRepository.softDelete(id)
    }

    private fun buildResponse(row: ResultRow): OfferResponse {
        val storeName = row[OffersTable.storeId]?.let { sid ->
            storeRepository.findById(sid.value)?.get(StoresTable.name)
        }

        return OfferResponse(
            id = row[OffersTable.id].value.toString(),
            storeId = row[OffersTable.storeId]?.value?.toString(),
            storeName = storeName,
            title = row[OffersTable.title],
            description = row[OffersTable.description],
            discountType = row[OffersTable.discountType],
            discountValue = row[OffersTable.discountValue],
            startDate = row[OffersTable.startDate],
            endDate = row[OffersTable.endDate],
            imageUrl = row[OffersTable.imageUrl],
            termsConditions = row[OffersTable.termsConditions],
            createdAt = row[OffersTable.createdAt]
        )
    }
}
