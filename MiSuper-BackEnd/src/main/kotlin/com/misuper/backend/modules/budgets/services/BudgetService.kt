package com.misuper.backend.modules.budgets.services

import com.misuper.backend.database.tables.BudgetItemsTable
import com.misuper.backend.database.tables.BudgetsTable
import com.misuper.backend.database.tables.CategoriesTable
import com.misuper.backend.exceptions.ForbiddenException
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.modules.budgets.dto.BudgetItemResponse
import com.misuper.backend.modules.budgets.dto.BudgetResponse
import com.misuper.backend.modules.budgets.dto.CreateBudgetRequest
import com.misuper.backend.modules.budgets.dto.UpdateBudgetRequest
import com.misuper.backend.modules.budgets.repositories.BudgetItemInsert
import com.misuper.backend.modules.budgets.repositories.BudgetRepository
import com.misuper.backend.modules.budgets.validators.BudgetValidator
import com.misuper.backend.modules.groups.repositories.GroupRepository
import org.jetbrains.exposed.v1.core.ResultRow
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class BudgetService(
    private val budgetRepository: BudgetRepository,
    private val groupRepository: GroupRepository
) {
    fun getByGroup(groupId: UUID, userId: UUID): List<BudgetResponse> {
        val group = groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        return budgetRepository.findByGroupId(groupId).map { row ->
            buildResponse(row)
        }
    }

    fun getById(budgetId: UUID, userId: UUID): BudgetResponse {
        val row = budgetRepository.findById(budgetId)
            ?: throw NotFoundException("Presupuesto no encontrado")

        val groupId = row[BudgetsTable.groupId].value
        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        return buildResponse(row)
    }

    fun create(userId: UUID, request: CreateBudgetRequest): BudgetResponse {
        BudgetValidator.validateCreate(request)

        val groupId = UUID.fromString(request.groupId)
        groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        val startDate = LocalDateTime.parse(request.startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val endDate = request.endDate?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }

        val items = request.items.map { item ->
            val categoryId = UUID.fromString(item.categoryId)
            budgetRepository.findCategoryById(categoryId)
                ?: throw NotFoundException("Categoría no encontrada: ${item.categoryId}")
            BudgetItemInsert(
                categoryId = categoryId,
                amount = BigDecimal.valueOf(item.amount)
            )
        }

        val budgetId = budgetRepository.createWithItems(
            groupIdVal = groupId,
            nameVal = request.name,
            totalAmountVal = BigDecimal.valueOf(request.totalAmount),
            periodVal = request.period.uppercase(),
            startDateVal = startDate,
            endDateVal = endDate,
            items = items
        )

        return getById(budgetId, userId)
    }

    fun update(id: UUID, userId: UUID, request: UpdateBudgetRequest): BudgetResponse {
        BudgetValidator.validateUpdate(request)

        val row = budgetRepository.findById(id)
            ?: throw NotFoundException("Presupuesto no encontrado")

        val groupId = row[BudgetsTable.groupId].value
        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        val startDate = request.startDate?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
        val endDate = request.endDate?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }

        val items = request.items?.map { item ->
            val categoryId = UUID.fromString(item.categoryId)
            budgetRepository.findCategoryById(categoryId)
                ?: throw NotFoundException("Categoría no encontrada: ${item.categoryId}")
            BudgetItemInsert(
                categoryId = categoryId,
                amount = BigDecimal.valueOf(item.amount)
            )
        }

        budgetRepository.updateWithItems(
            id = id,
            nameVal = request.name,
            totalAmountVal = request.totalAmount?.let { BigDecimal.valueOf(it) },
            periodVal = request.period?.uppercase(),
            startDateVal = startDate,
            endDateVal = endDate,
            items = items
        )

        return getById(id, userId)
    }

    fun activate(id: UUID, userId: UUID): BudgetResponse {
        val row = budgetRepository.findById(id)
            ?: throw NotFoundException("Presupuesto no encontrado")

        val groupId = row[BudgetsTable.groupId].value
        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        budgetRepository.activate(id, groupId)
        return getById(id, userId)
    }

    fun softDelete(id: UUID, userId: UUID) {
        val row = budgetRepository.findById(id)
            ?: throw NotFoundException("Presupuesto no encontrado")

        val groupId = row[BudgetsTable.groupId].value
        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        budgetRepository.softDelete(id)
    }

    private fun buildResponse(row: ResultRow): BudgetResponse {
        val budgetId = row[BudgetsTable.id].value
        val groupId = row[BudgetsTable.groupId].value

        val items = budgetRepository.getItems(budgetId).map { item ->
            val categoryId = item[BudgetItemsTable.categoryId].value
            val categoryRow = budgetRepository.findCategoryById(categoryId)
            BudgetItemResponse(
                id = item[BudgetItemsTable.id].value.toString(),
                categoryId = categoryId.toString(),
                categoryName = categoryRow?.get(CategoriesTable.name) ?: "Desconocida",
                amount = item[BudgetItemsTable.amount]
            )
        }

        return BudgetResponse(
            id = budgetId.toString(),
            groupId = groupId.toString(),
            name = row[BudgetsTable.name],
            totalAmount = row[BudgetsTable.totalAmount],
            period = row[BudgetsTable.period],
            startDate = row[BudgetsTable.startDate],
            endDate = row[BudgetsTable.endDate],
            items = items,
            createdAt = row[BudgetsTable.createdAt]
        )
    }
}
