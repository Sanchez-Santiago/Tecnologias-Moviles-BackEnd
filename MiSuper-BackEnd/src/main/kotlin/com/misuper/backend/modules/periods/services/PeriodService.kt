package com.misuper.backend.modules.periods.services

import com.misuper.backend.database.tables.GroupMonthlyPeriodsTable
import com.misuper.backend.database.tables.GroupMembersTable
import com.misuper.backend.exceptions.ConflictException
import com.misuper.backend.exceptions.ForbiddenException
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.modules.groups.repositories.GroupRepository
import com.misuper.backend.modules.periods.dto.ClosePeriodRequest
import com.misuper.backend.modules.periods.dto.CreatePeriodRequest
import com.misuper.backend.modules.periods.dto.PeriodResponse
import com.misuper.backend.modules.periods.repositories.PeriodRepository
import com.misuper.backend.modules.periods.validators.PeriodValidator
import java.time.LocalDateTime
import java.util.UUID

class PeriodService(
    private val periodRepository: PeriodRepository,
    private val groupRepository: GroupRepository
) {

    fun getCurrentPeriod(groupId: UUID, userId: UUID): PeriodResponse {
        val group = groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        val row = periodRepository.findByGroupAndStatus(groupId, "OPEN")
            ?: throw NotFoundException("No hay un período abierto para este grupo")

        return toResponse(row)
    }

    fun getPeriods(groupId: UUID, userId: UUID): List<PeriodResponse> {
        val group = groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        return periodRepository.findByGroup(groupId).map { toResponse(it) }
    }

    fun getPeriodById(periodId: UUID, userId: UUID): PeriodResponse {
        val row = periodRepository.findById(periodId)
            ?: throw NotFoundException("Período no encontrado")

        val groupId = row[GroupMonthlyPeriodsTable.groupId].value
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        return toResponse(row)
    }

    fun createPeriod(groupId: UUID, userId: UUID, request: CreatePeriodRequest): PeriodResponse {
        PeriodValidator.validateCreate(request)

        val group = groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")
        if (memberRole[GroupMembersTable.role] !in listOf("OWNER", "ADMIN")) {
            throw ForbiddenException("Se requiere rol de administrador del grupo")
        }

        // Close current open period if exists
        val currentOpen = periodRepository.findByGroupAndStatus(groupId, "OPEN")
        if (currentOpen != null) {
            periodRepository.close(
                periodId = currentOpen[GroupMonthlyPeriodsTable.id].value,
                closedByUserId = userId,
                finalBalance = null
            )
        }

        val periodId = periodRepository.create(
            groupId = groupId,
            name = request.name,
            startDate = LocalDateTime.now(),
            cycleType = request.cycleType
        )

        val row = periodRepository.findById(periodId)!!
        return toResponse(row)
    }

    fun closePeriod(periodId: UUID, userId: UUID, request: ClosePeriodRequest): PeriodResponse {
        val row = periodRepository.findById(periodId)
            ?: throw NotFoundException("Período no encontrado")

        val groupId = row[GroupMonthlyPeriodsTable.groupId].value
        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        if (row[GroupMonthlyPeriodsTable.status] == "CLOSED") {
            throw ConflictException("El período ya está cerrado")
        }

        periodRepository.close(
            periodId = periodId,
            closedByUserId = userId,
            finalBalance = request.finalBalance
        )

        val updated = periodRepository.findById(periodId)!!
        return toResponse(updated)
    }

    private fun toResponse(row: org.jetbrains.exposed.v1.core.ResultRow): PeriodResponse {
        return PeriodResponse(
            id = row[GroupMonthlyPeriodsTable.id].value.toString(),
            groupId = row[GroupMonthlyPeriodsTable.groupId].value.toString(),
            name = row[GroupMonthlyPeriodsTable.name],
            startDate = row[GroupMonthlyPeriodsTable.startDate],
            endDate = row[GroupMonthlyPeriodsTable.endDate],
            status = row[GroupMonthlyPeriodsTable.status],
            initialBalance = row[GroupMonthlyPeriodsTable.initialBalance],
            finalBalance = row[GroupMonthlyPeriodsTable.finalBalance],
            createdAt = row[GroupMonthlyPeriodsTable.createdAt],
            closedBy = row[GroupMonthlyPeriodsTable.closedBy]?.let { it.value.toString() },
            updatedAt = row[GroupMonthlyPeriodsTable.updatedAt],
            cycleType = row[GroupMonthlyPeriodsTable.cycleType]
        )
    }
}
