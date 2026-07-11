package com.misuper.backend.modules.budgets.services

import com.misuper.backend.database.tables.BudgetsTable
import com.misuper.backend.exceptions.ForbiddenException
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.modules.budgets.dto.BudgetResponse
import com.misuper.backend.modules.budgets.dto.CreateBudgetRequest
import com.misuper.backend.modules.budgets.dto.UpdateBudgetRequest
import com.misuper.backend.modules.budgets.repositories.BudgetRepository
import com.misuper.backend.modules.budgets.validators.BudgetValidator
import com.misuper.backend.modules.groups.repositories.GroupRepository
import org.jetbrains.exposed.v1.core.ResultRow
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class BudgetService(
    private val budgetRepository: BudgetRepository,
    private val groupRepository: GroupRepository
) {
    fun getByGroup(groupId: UUID, userId: UUID): List<BudgetResponse> {
        groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        return budgetRepository.findByGroupId(groupId).map { buildResponse(it) }
    }

    fun getCurrent(groupId: UUID, userId: UUID): BudgetResponse? {
        groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        val row = budgetRepository.findCurrentByGroupId(groupId)
        return row?.let { buildResponse(it) }
    }

    fun create(userId: UUID, request: CreateBudgetRequest): BudgetResponse {
        BudgetValidator.validateCreate(request)

        val groupId = UUID.fromString(request.groupId)
        groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        val startDate = LocalDate.parse(request.startDate)
        val endDate = LocalDate.parse(request.endDate)

        val budgetId = budgetRepository.create(
            groupId = groupId,
            startDate = startDate,
            endDate = endDate,
            total = BigDecimal.valueOf(request.total),
            createdBy = userId
        )

        val row = budgetRepository.findById(budgetId)!!
        return buildResponse(row)
    }

    fun update(id: UUID, userId: UUID, request: UpdateBudgetRequest): BudgetResponse {
        BudgetValidator.validateUpdate(request)

        val row = budgetRepository.findById(id)
            ?: throw NotFoundException("Presupuesto no encontrado")

        val groupId = row[BudgetsTable.groupId].value
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        budgetRepository.update(
            id = id,
            total = request.total?.let { BigDecimal.valueOf(it) },
            startDate = request.startDate?.let { LocalDate.parse(it) },
            endDate = request.endDate?.let { LocalDate.parse(it) }
        )

        val updated = budgetRepository.findById(id)!!
        return buildResponse(updated)
    }

    fun delete(id: UUID, userId: UUID) {
        val row = budgetRepository.findById(id)
            ?: throw NotFoundException("Presupuesto no encontrado")

        val groupId = row[BudgetsTable.groupId].value
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        budgetRepository.delete(id)
    }

    private fun buildResponse(row: ResultRow): BudgetResponse {
        return BudgetResponse(
            id = row[BudgetsTable.id].value.toString(),
            groupId = row[BudgetsTable.groupId].value.toString(),
            startDate = row[BudgetsTable.startDate].toString(),
            endDate = row[BudgetsTable.endDate].toString(),
            total = row[BudgetsTable.total],
            createdBy = row[BudgetsTable.createdBy]?.let { it.value.toString() },
            createdAt = row[BudgetsTable.createdAt],
            updatedAt = row[BudgetsTable.updatedAt]
        )
    }
}
