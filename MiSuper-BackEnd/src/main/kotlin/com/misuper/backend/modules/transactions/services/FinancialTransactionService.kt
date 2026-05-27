package com.misuper.backend.modules.transactions.services

import com.misuper.backend.database.tables.FinancialTransactionsTable
import com.misuper.backend.database.tables.UsersTable
import com.misuper.backend.exceptions.ForbiddenException
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.groups.repositories.GroupRepository
import com.misuper.backend.modules.transactions.dto.CreateFinancialTransactionRequest
import com.misuper.backend.modules.transactions.dto.FinancialSummaryResponse
import com.misuper.backend.modules.transactions.dto.FinancialTransactionResponse
import com.misuper.backend.modules.transactions.repositories.FinancialTransactionRepository
import org.jetbrains.exposed.v1.core.ResultRow
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class FinancialTransactionService(
    private val repository: FinancialTransactionRepository,
    private val groupRepository: GroupRepository
) {
    fun getByGroup(groupId: UUID, userId: UUID): List<FinancialTransactionResponse> {
        checkMembership(groupId, userId)
        return repository.findByGroup(groupId).map { buildResponse(it) }
    }

    fun create(userId: UUID, request: CreateFinancialTransactionRequest): FinancialTransactionResponse {
        validate(request)
        val groupId = UUID.fromString(request.groupId)
        checkMembership(groupId, userId)

        val id = repository.create(
            groupId = groupId,
            userId = userId,
            type = request.type.uppercase(),
            category = request.category.trim(),
            amount = BigDecimal.valueOf(request.amount),
            description = request.description,
            transactionDate = request.transactionDate?.let {
                LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            } ?: LocalDateTime.now()
        )

        return buildResponse(repository.findById(id)!!)
    }

    fun delete(id: UUID, userId: UUID) {
        val row = repository.findById(id) ?: throw NotFoundException("Movimiento no encontrado")
        checkMembership(row[FinancialTransactionsTable.groupId].value, userId)
        repository.softDelete(id)
    }

    fun summary(groupId: UUID, userId: UUID): FinancialSummaryResponse {
        checkMembership(groupId, userId)
        val rows = repository.findByGroup(groupId)
        val income = rows
            .filter { it[FinancialTransactionsTable.type] == "INCOME" }
            .sumOf { it[FinancialTransactionsTable.amount] }
        val expense = rows
            .filter { it[FinancialTransactionsTable.type] == "EXPENSE" }
            .sumOf { it[FinancialTransactionsTable.amount] }
        return FinancialSummaryResponse(income = income, expense = expense, balance = income - expense)
    }

    private fun validate(request: CreateFinancialTransactionRequest) {
        if (request.groupId.isBlank()) throw ValidationException("El grupo es obligatorio")
        if (request.category.isBlank()) throw ValidationException("La categoría es obligatoria")
        if (request.amount <= 0) throw ValidationException("El monto debe ser mayor a cero")
        if (request.type.uppercase() !in setOf("INCOME", "EXPENSE")) {
            throw ValidationException("El tipo debe ser INCOME o EXPENSE")
        }
    }

    private fun checkMembership(groupId: UUID, userId: UUID) {
        groupRepository.findById(groupId) ?: throw NotFoundException("Grupo no encontrado")
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")
    }

    private fun buildResponse(row: ResultRow): FinancialTransactionResponse {
        val userId = row[FinancialTransactionsTable.userId].value
        val userRow = groupRepository.findUserById(userId)
        return FinancialTransactionResponse(
            id = row[FinancialTransactionsTable.id].value.toString(),
            groupId = row[FinancialTransactionsTable.groupId].value.toString(),
            userId = userId.toString(),
            userName = userRow?.get(UsersTable.fullName) ?: "Desconocido",
            type = row[FinancialTransactionsTable.type],
            category = row[FinancialTransactionsTable.category],
            amount = row[FinancialTransactionsTable.amount],
            description = row[FinancialTransactionsTable.description],
            transactionDate = row[FinancialTransactionsTable.transactionDate],
            createdAt = row[FinancialTransactionsTable.createdAt]
        )
    }
}
