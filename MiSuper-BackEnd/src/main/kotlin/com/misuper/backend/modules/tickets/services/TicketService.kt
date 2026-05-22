package com.misuper.backend.modules.tickets.services

import com.misuper.backend.database.tables.TicketMessagesTable
import com.misuper.backend.database.tables.TicketsTable
import com.misuper.backend.database.tables.UsersTable
import com.misuper.backend.exceptions.ForbiddenException
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.modules.groups.repositories.GroupRepository
import com.misuper.backend.modules.tickets.dto.AddMessageRequest
import com.misuper.backend.modules.tickets.dto.CreateTicketRequest
import com.misuper.backend.modules.tickets.dto.TicketMessageResponse
import com.misuper.backend.modules.tickets.dto.TicketResponse
import com.misuper.backend.modules.tickets.dto.UpdateTicketRequest
import com.misuper.backend.modules.tickets.repositories.TicketRepository
import com.misuper.backend.modules.tickets.validators.TicketValidator
import org.jetbrains.exposed.v1.core.ResultRow
import java.util.UUID

class TicketService(
    private val ticketRepository: TicketRepository,
    private val groupRepository: GroupRepository
) {
    fun getByGroup(groupId: UUID, userId: UUID): List<TicketResponse> {
        val group = groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        return ticketRepository.findByGroupId(groupId).map { row ->
            buildResponse(row)
        }
    }

    fun getById(ticketId: UUID, userId: UUID): TicketResponse {
        val row = ticketRepository.findById(ticketId)
            ?: throw NotFoundException("Ticket no encontrado")

        val groupId = row[TicketsTable.groupId].value
        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        return buildResponse(row)
    }

    fun create(userId: UUID, request: CreateTicketRequest): TicketResponse {
        TicketValidator.validateCreate(request)

        val groupId = UUID.fromString(request.groupId)
        groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        val assignedTo = request.assignedTo?.let { UUID.fromString(it) }
        assignedTo?.let { assignedId ->
            val assignedRow = ticketRepository.findUserById(assignedId)
                ?: throw NotFoundException("Usuario asignado no encontrado")
            val assignedMember = groupRepository.getMemberRole(groupId, assignedId)
                ?: throw NotFoundException("El usuario asignado no es miembro del grupo")
        }

        val ticketId = ticketRepository.create(
            groupIdVal = groupId,
            createdByVal = userId,
            titleVal = request.title,
            descriptionVal = request.description,
            priorityVal = request.priority?.uppercase() ?: "MEDIUM",
            assignedToVal = assignedTo
        )

        return getById(ticketId, userId)
    }

    fun update(id: UUID, userId: UUID, request: UpdateTicketRequest): TicketResponse {
        TicketValidator.validateUpdate(request)

        val row = ticketRepository.findById(id)
            ?: throw NotFoundException("Ticket no encontrado")

        val groupId = row[TicketsTable.groupId].value
        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        val assignedTo = request.assignedTo?.let { UUID.fromString(it) }
        assignedTo?.let { assignedId ->
            val assignedRow = ticketRepository.findUserById(assignedId)
                ?: throw NotFoundException("Usuario asignado no encontrado")
            val assignedMember = groupRepository.getMemberRole(groupId, assignedId)
                ?: throw NotFoundException("El usuario asignado no es miembro del grupo")
        }

        ticketRepository.update(
            id = id,
            titleVal = request.title,
            descriptionVal = request.description,
            statusVal = request.status?.uppercase(),
            priorityVal = request.priority?.uppercase(),
            assignedToVal = assignedTo
        )

        return getById(id, userId)
    }

    fun softDelete(id: UUID, userId: UUID) {
        val row = ticketRepository.findById(id)
            ?: throw NotFoundException("Ticket no encontrado")

        val groupId = row[TicketsTable.groupId].value
        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        ticketRepository.softDelete(id)
    }

    fun addMessage(ticketId: UUID, userId: UUID, request: AddMessageRequest): TicketMessageResponse {
        TicketValidator.validateMessage(request)

        val row = ticketRepository.findById(ticketId)
            ?: throw NotFoundException("Ticket no encontrado")

        val groupId = row[TicketsTable.groupId].value
        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        ticketRepository.addMessage(ticketId, userId, request.message)

        val messages = ticketRepository.getMessages(ticketId)
        val lastMessage = messages.last()
        return buildMessageResponse(lastMessage)
    }

    private fun buildResponse(row: ResultRow): TicketResponse {
        val ticketId = row[TicketsTable.id].value
        val createdBy = row[TicketsTable.createdBy].value
        val createdByUser = ticketRepository.findUserById(createdBy)
        val createdByName = createdByUser?.get(UsersTable.fullName) ?: "Desconocido"

        val assignedTo = row[TicketsTable.assignedTo]?.let { aid ->
            val userRow = ticketRepository.findUserById(aid.value)
            aid.value to (userRow?.get(UsersTable.fullName) ?: "Desconocido")
        }

        val messages = ticketRepository.getMessages(ticketId).map { msg ->
            buildMessageResponse(msg)
        }

        return TicketResponse(
            id = ticketId.toString(),
            groupId = row[TicketsTable.groupId].value.toString(),
            createdBy = createdBy.toString(),
            createdByName = createdByName,
            title = row[TicketsTable.title],
            description = row[TicketsTable.description],
            status = row[TicketsTable.status],
            priority = row[TicketsTable.priority],
            assignedTo = assignedTo?.first?.toString(),
            assignedToName = assignedTo?.second,
            messages = messages,
            createdAt = row[TicketsTable.createdAt],
            updatedAt = row[TicketsTable.updatedAt]
        )
    }

    private fun buildMessageResponse(row: ResultRow): TicketMessageResponse {
        val userId = row[TicketMessagesTable.userId].value
        val userRow = ticketRepository.findUserById(userId)
        val userName = userRow?.get(UsersTable.fullName) ?: "Desconocido"

        return TicketMessageResponse(
            id = row[TicketMessagesTable.id].value.toString(),
            userId = userId.toString(),
            userName = userName,
            message = row[TicketMessagesTable.message],
            createdAt = row[TicketMessagesTable.createdAt]
        )
    }
}
