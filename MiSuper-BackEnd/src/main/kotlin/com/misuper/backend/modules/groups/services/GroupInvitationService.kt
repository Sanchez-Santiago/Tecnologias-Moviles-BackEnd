package com.misuper.backend.modules.groups.services

import com.misuper.backend.database.tables.GroupInvitationsTable
import com.misuper.backend.database.tables.GroupMembersTable
import com.misuper.backend.database.tables.GroupsTable
import com.misuper.backend.database.tables.UsersTable
import com.misuper.backend.exceptions.ConflictException
import com.misuper.backend.exceptions.ForbiddenException
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.modules.auth.repositories.AuthRepository
import com.misuper.backend.modules.groups.dto.InvitationResponse
import com.misuper.backend.modules.groups.dto.InviteRequest
import com.misuper.backend.modules.groups.repositories.GroupInvitationRepository
import com.misuper.backend.modules.groups.repositories.GroupRepository
import com.misuper.backend.modules.notifications.services.NotificationService
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.LocalDateTime
import java.util.UUID

class GroupInvitationService(
    private val groupInvitationRepository: GroupInvitationRepository,
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository,
    private val notificationService: NotificationService
) {

    fun invite(groupId: UUID, requesterId: UUID, request: InviteRequest): InvitationResponse {
        val group = groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        if (group[GroupsTable.categoria] == "INDIVIDUAL") {
            throw ForbiddenException("No puedes invitar miembros a un grupo individual")
        }

        requireAdmin(groupId, requesterId)

        val targetUser = authRepository.findByEmail(request.email.lowercase())
            ?: throw NotFoundException("Usuario no encontrado con ese email")

        val targetUserId = targetUser[UsersTable.id].value

        val existingMember = groupRepository.getMemberRole(groupId, targetUserId)
        if (existingMember != null) {
            throw ConflictException("El usuario ya es miembro del grupo")
        }

        val existingInvitation = groupInvitationRepository.findByGroupAndEmail(groupId, request.email)
        if (existingInvitation != null) {
            throw ConflictException("Ya existe una invitación pendiente para este email")
        }

        groupInvitationRepository.create(groupId, request.email, requesterId)
        val row = groupInvitationRepository.findByGroupAndEmail(groupId, request.email)!!

        notificationService.create(
            userId = targetUserId,
            type = "INVITATION",
            title = "Nueva invitación a grupo",
            message = "Has sido invitado al grupo '${group[GroupsTable.name]}'",
            data = """{"groupId":"$groupId"}"""
        )

        return toResponse(row)
    }

    fun getMyInvitations(userId: UUID): List<InvitationResponse> {
        val user = authRepository.findById(userId)
            ?: throw NotFoundException("Usuario no encontrado")

        val email = user[UsersTable.email]
        val rows = groupInvitationRepository.findPendingByEmail(email)

        return rows.map { toResponse(it) }
    }

    fun accept(token: String, userId: UUID): InvitationResponse {
        val row = groupInvitationRepository.findByToken(token)
            ?: throw NotFoundException("Invitación no encontrada")

        if (row[GroupInvitationsTable.status] != "PENDING") {
            throw ConflictException("La invitación ya fue procesada")
        }

        if (row[GroupInvitationsTable.expiresAt].isBefore(LocalDateTime.now())) {
            groupInvitationRepository.updateStatus(row[GroupInvitationsTable.id].value, "EXPIRED")
            throw ConflictException("La invitación ha expirado")
        }

        validateInvitedUser(row, userId)

        val groupId = row[GroupInvitationsTable.groupId].value
        groupRepository.addMember(groupId, userId)

        groupInvitationRepository.updateStatus(row[GroupInvitationsTable.id].value, "ACCEPTED")

        val inviterId = row[GroupInvitationsTable.invitedBy].value
        val groupName = groupRepository.findById(groupId)?.get(GroupsTable.name) ?: ""
        notificationService.create(
            userId = inviterId,
            type = "INVITATION",
            title = "Invitación aceptada",
            message = "Un usuario aceptó tu invitación al grupo '$groupName'",
            data = """{"groupId":"$groupId"}"""
        )

        val updated = groupInvitationRepository.findByToken(token)!!
        return toResponse(updated)
    }

    fun reject(token: String, userId: UUID): InvitationResponse {
        val row = groupInvitationRepository.findByToken(token)
            ?: throw NotFoundException("Invitación no encontrada")

        if (row[GroupInvitationsTable.status] != "PENDING") {
            throw ConflictException("La invitación ya fue procesada")
        }

        validateInvitedUser(row, userId)

        groupInvitationRepository.updateStatus(row[GroupInvitationsTable.id].value, "REJECTED")

        val updated = groupInvitationRepository.findByToken(token)!!
        return toResponse(updated)
    }

    private fun validateInvitedUser(row: ResultRow, userId: UUID) {
        val user = authRepository.findById(userId)
            ?: throw NotFoundException("Usuario no encontrado")

        val invitedEmail = row[GroupInvitationsTable.invitedEmail]
        if (user[UsersTable.email].lowercase() != invitedEmail.lowercase()) {
            throw ForbiddenException("Esta invitación no está dirigida a tu usuario")
        }
    }

    private fun requireAdmin(groupId: UUID, userId: UUID) {
        val member = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")
        if (member[GroupMembersTable.role] != "ADMIN") {
            throw ForbiddenException("Se requiere rol de administrador del grupo")
        }
    }

    private fun toResponse(row: ResultRow): InvitationResponse {
        val groupRow = groupRepository.findById(row[GroupInvitationsTable.groupId].value)
            ?: throw NotFoundException("Grupo no encontrado")

        val inviterRow = authRepository.findById(row[GroupInvitationsTable.invitedBy].value)
        val inviterEmail = inviterRow?.get(UsersTable.email) ?: ""

        return InvitationResponse(
            id = row[GroupInvitationsTable.id].value.toString(),
            groupId = row[GroupInvitationsTable.groupId].value.toString(),
            groupName = groupRow[GroupsTable.name],
            invitedBy = row[GroupInvitationsTable.invitedBy].value.toString(),
            invitedByEmail = inviterEmail,
            status = row[GroupInvitationsTable.status],
            token = row[GroupInvitationsTable.token],
            expiresAt = row[GroupInvitationsTable.expiresAt],
            createdAt = row[GroupInvitationsTable.createdAt]
        )
    }
}
