package com.misuper.backend.modules.tickets.services

import com.misuper.backend.database.tables.*
import com.misuper.backend.exceptions.ForbiddenException
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.groups.repositories.GroupRepository
import com.misuper.backend.modules.tickets.dto.*
import com.misuper.backend.modules.tickets.repositories.TicketRepository
import org.jetbrains.exposed.v1.core.ResultRow
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.UUID

class TicketService(
    private val ticketRepository: TicketRepository,
    private val groupRepository: GroupRepository
) {
    fun getByGroup(groupId: UUID, userId: UUID, from: LocalDate? = null, to: LocalDate? = null): List<TicketResponse> {
        groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        val (fromDate, toDate) = resolveDateRange(from, to)

        return ticketRepository.findByGroupIdAndDateRange(groupId, fromDate, toDate).map { buildResponse(it) }
    }

    fun getById(ticketId: UUID, userId: UUID): TicketResponse {
        val row = ticketRepository.findById(ticketId)
            ?: throw NotFoundException("Ticket no encontrado")

        val groupId = row[TicketsTable.groupId].value
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        return buildResponse(row)
    }

    fun create(userId: UUID, request: CreateTicketRequest): TicketResponse {
        if (request.amount <= 0) {
            throw ValidationException("El monto debe ser mayor que cero")
        }
        if (request.movementType !in listOf("INCOME", "EXPENSE")) {
            throw ValidationException("Tipo de movimiento inválido: debe ser INCOME o EXPENSE")
        }

        val groupId = UUID.fromString(request.groupId)
        groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        val purchaseDate = if (request.purchaseDate != null) {
            LocalDateTime.parse(request.purchaseDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } else {
            LocalDateTime.now()
        }

        val ticketId = ticketRepository.create(
            groupIdVal = groupId,
            uploadedByVal = userId,
            supermarketNameVal = request.supermarketName,
            amountVal = BigDecimal.valueOf(request.amount),
            movementTypeVal = request.movementType,
            purchaseDateVal = purchaseDate,
            commentVal = request.comment,
            imageUrlVal = request.imageUrl
        )

        request.products?.forEach { product ->
            val productId = product.productId?.let { UUID.fromString(it) }
            ticketRepository.addProduct(
                ticketIdVal = ticketId,
                productIdVal = productId,
                detectedNameVal = product.productName,
                quantityVal = product.quantity?.let { BigDecimal.valueOf(it) },
                priceVal = product.price?.let { BigDecimal.valueOf(it) },
                brandVal = product.brand
            )
        }

        return getById(ticketId, userId)
    }

    fun update(ticketId: UUID, userId: UUID, request: UpdateTicketRequest): TicketResponse {
        val row = ticketRepository.findById(ticketId)
            ?: throw NotFoundException("Ticket no encontrado")

        val groupId = row[TicketsTable.groupId].value
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        ticketRepository.update(
            id = ticketId,
            supermarketNameVal = request.supermarketName,
            commentVal = request.comment,
            imageUrlVal = request.imageUrl
        )

        return getById(ticketId, userId)
    }

    fun delete(ticketId: UUID, userId: UUID) {
        val row = ticketRepository.findById(ticketId)
            ?: throw NotFoundException("Ticket no encontrado")

        val groupId = row[TicketsTable.groupId].value
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        ticketRepository.delete(ticketId)
    }

    fun resolveDateRange(from: LocalDate?, to: LocalDate?): Pair<LocalDate, LocalDate> {
        val fromDate = from ?: LocalDate.now().withDayOfMonth(1)
        val toDate = to ?: LocalDate.now()
        return fromDate to toDate
    }

    companion object {
        fun parsePeriodToDateRange(period: String): Pair<LocalDate, LocalDate> {
            val today = LocalDate.now()
            return when (period.lowercase()) {
                "this-week" -> {
                    val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val end = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                    start to end
                }
                "this-month" -> today.withDayOfMonth(1) to today.withDayOfMonth(today.lengthOfMonth())
                "this-year" -> today.withDayOfYear(1) to today.withDayOfYear(today.lengthOfYear())
                else -> throw IllegalArgumentException("Período inválido: $period")
            }
        }
    }

    private fun buildResponse(row: ResultRow): TicketResponse {
        val ticketId = row[TicketsTable.id].value
        val groupId = row[TicketsTable.groupId].value
        val uploadedById = row[TicketsTable.uploadedBy].value
        val userRow = groupRepository.findUserById(uploadedById)

        val products = ticketRepository.getProducts(ticketId).map { prod ->
            val productId = prod[TicketProductsTable.productId]?.value
            TicketProductResponse(
                id = prod[TicketProductsTable.id].value.toString(),
                productId = productId?.toString(),
                productName = prod[TicketProductsTable.detectedName],
                quantity = prod[TicketProductsTable.quantity]?.toDouble(),
                price = prod[TicketProductsTable.price]?.toDouble(),
                brand = prod[TicketProductsTable.brand]
            )
        }

        return TicketResponse(
            id = ticketId.toString(),
            groupId = groupId.toString(),
            uploadedBy = uploadedById.toString(),
            uploadedByName = userRow?.get(UsersTable.fullName) ?: "Usuario",
            supermarketName = row[TicketsTable.supermarketName],
            amount = row[TicketsTable.amount].toDouble(),
            movementType = row[TicketsTable.movementType],
            purchaseDate = row[TicketsTable.purchaseDate].toString(),
            comment = row[TicketsTable.comment],
            imageUrl = row[TicketsTable.imageUrl],
            status = row[TicketsTable.status],
            products = products,
            createdAt = row[TicketsTable.createdAt],
            updatedAt = row[TicketsTable.updatedAt]
        )
    }
}
