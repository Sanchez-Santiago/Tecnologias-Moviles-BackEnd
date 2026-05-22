package com.misuper.backend.modules.groups.services

import com.misuper.backend.database.tables.GroupMembersTable
import com.misuper.backend.database.tables.GroupsTable
import com.misuper.backend.database.tables.UsersTable
import com.misuper.backend.exceptions.ForbiddenException
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.modules.auth.repositories.AuthRepository
import com.misuper.backend.modules.groups.dto.*
import com.misuper.backend.modules.groups.repositories.GroupRepository
import com.misuper.backend.modules.groups.validators.GroupValidator
import java.util.UUID

class GroupService(
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository
) {
    fun getMyGroups(userId: UUID): List<GroupResponse> {
        val rows = groupRepository.findByUserId(userId)
        return rows.map { row ->
            val groupId = row[GroupsTable.id].value
            val memberRow = groupRepository.getMemberRole(groupId, userId)
            GroupResponse(
                id = groupId.toString(),
                name = row[GroupsTable.name],
                description = row[GroupsTable.description],
                createdBy = row[GroupsTable.createdBy].value.toString(),
                memberCount = groupRepository.countMembers(groupId),
                role = memberRow?.get(GroupMembersTable.role) ?: "MEMBER",
                createdAt = row[GroupsTable.createdAt]
            )
        }
    }

    fun getById(groupId: UUID, userId: UUID): GroupDetailResponse {
        val row = groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        val members = groupRepository.getMembers(groupId).mapNotNull { m ->
            val memberUserId = m[GroupMembersTable.userId].value
            val userRow = groupRepository.findUserById(memberUserId) ?: return@mapNotNull null
            GroupMemberResponse(
                id = memberUserId.toString(),
                fullName = userRow[UsersTable.fullName],
                email = userRow[UsersTable.email],
                role = m[GroupMembersTable.role],
                joinedAt = m[GroupMembersTable.joinedAt]
            )
        }

        return GroupDetailResponse(
            id = row[GroupsTable.id].value.toString(),
            name = row[GroupsTable.name],
            description = row[GroupsTable.description],
            createdBy = row[GroupsTable.createdBy].value.toString(),
            members = members,
            createdAt = row[GroupsTable.createdAt]
        )
    }

    fun create(userId: UUID, request: CreateGroupRequest): GroupResponse {
        GroupValidator.validateCreate(request)

        val groupId = groupRepository.create(
            nameVal = request.name,
            descriptionVal = request.description,
            createdByVal = userId
        )

        groupRepository.addMember(groupId, userId, "ADMIN")

        val row = groupRepository.findById(groupId)!!
        return GroupResponse(
            id = groupId.toString(),
            name = row[GroupsTable.name],
            description = row[GroupsTable.description],
            createdBy = userId.toString(),
            memberCount = 1,
            role = "ADMIN",
            createdAt = row[GroupsTable.createdAt]
        )
    }

    fun update(groupId: UUID, userId: UUID, request: UpdateGroupRequest): GroupResponse {
        val row = groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        requireAdmin(groupId, userId)

        groupRepository.update(
            id = groupId,
            nameVal = request.name,
            descriptionVal = request.description
        )

        val updated = groupRepository.findById(groupId)!!
        return GroupResponse(
            id = groupId.toString(),
            name = updated[GroupsTable.name],
            description = updated[GroupsTable.description],
            createdBy = updated[GroupsTable.createdBy].value.toString(),
            memberCount = groupRepository.countMembers(groupId),
            role = "ADMIN",
            createdAt = updated[GroupsTable.createdAt]
        )
    }

    fun delete(groupId: UUID, userId: UUID) {
        groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        requireAdmin(groupId, userId)
        groupRepository.softDelete(groupId)
    }

    fun addMember(groupId: UUID, userId: UUID, request: AddMemberRequest): GroupDetailResponse {
        val group = groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        requireAdmin(groupId, userId)

        val targetUser = authRepository.findByEmail(request.email.lowercase())
            ?: throw NotFoundException("Usuario no encontrado con ese email")

        val targetUserId = targetUser[UsersTable.id].value

        val existingMember = groupRepository.getMemberRole(groupId, targetUserId)
        if (existingMember != null) {
            throw com.misuper.backend.exceptions.ConflictException("El usuario ya es miembro del grupo")
        }

        groupRepository.addMember(groupId, targetUserId)

        return getById(groupId, userId)
    }

    fun removeMember(groupId: UUID, requesterId: UUID, memberId: UUID) {
        groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        requireAdmin(groupId, requesterId)

        groupRepository.getMemberRole(groupId, memberId)
            ?: throw NotFoundException("El usuario no es miembro del grupo")

        groupRepository.removeMember(groupId, memberId)
    }

    private fun requireAdmin(groupId: UUID, userId: UUID) {
        val member = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")
        if (member[GroupMembersTable.role] != "ADMIN") {
            throw ForbiddenException("Se requiere rol de administrador del grupo")
        }
    }
}
