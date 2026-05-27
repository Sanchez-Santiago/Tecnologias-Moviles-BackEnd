package com.misuper.backend.modules.groups.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.GroupMembersTable
import com.misuper.backend.database.tables.GroupsTable
import com.misuper.backend.database.tables.UsersTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime
import java.util.UUID

class GroupRepository {

    private val db get() = DatabaseFactory.getDatabase()

    fun findById(id: UUID): ResultRow? = transaction(db) {
        GroupsTable.selectAll().where { GroupsTable.id eq id }.singleOrNull()
    }

    fun findByUserId(userId: UUID): List<ResultRow> = transaction(db) {
        val groupIds = GroupMembersTable.selectAll()
            .where { GroupMembersTable.userId eq EntityID(userId, UsersTable) }
            .map { it[GroupMembersTable.groupId].value }
            .toSet()
        GroupsTable.selectAll()
            .where { GroupsTable.active eq true }
            .toList()
            .filter { row -> row[GroupsTable.id].value in groupIds }
    }

    fun create(nameVal: String, descriptionVal: String?, createdByVal: UUID): UUID = transaction(db) {
        GroupsTable.insert {
            it[name] = nameVal
            it[description] = descriptionVal
            it[createdBy] = EntityID(createdByVal, UsersTable)
        }[GroupsTable.id].value
    }

    fun update(id: UUID, nameVal: String?, descriptionVal: String?) = transaction(db) {
        GroupsTable.update({ GroupsTable.id eq id }) { stmt ->
            nameVal?.let { stmt[name] = it }
            descriptionVal?.let { stmt[description] = it }
            stmt[updatedAt] = LocalDateTime.now()
        }
    }

    fun softDelete(id: UUID) = transaction(db) {
        GroupsTable.update({ GroupsTable.id eq id }) { stmt ->
            stmt[active] = false
            stmt[updatedAt] = LocalDateTime.now()
        }
    }

    fun countMembers(groupId: UUID): Int = transaction(db) {
        GroupMembersTable.selectAll()
            .where { GroupMembersTable.groupId eq EntityID(groupId, GroupsTable) }
            .count().toInt()
    }

    fun getMembers(groupId: UUID): List<ResultRow> = transaction(db) {
        GroupMembersTable.selectAll()
            .where { GroupMembersTable.groupId eq EntityID(groupId, GroupsTable) }
            .toList()
    }

    fun getMemberRole(groupIdVal: UUID, userIdVal: UUID): ResultRow? = transaction(db) {
        val rows = GroupMembersTable.selectAll().where {
            GroupMembersTable.groupId eq EntityID(groupIdVal, GroupsTable)
        }.toList()
        rows.firstOrNull { it[GroupMembersTable.userId].value == userIdVal }
    }

    fun addMember(groupIdVal: UUID, userIdVal: UUID, roleVal: String = "MEMBER") = transaction(db) {
        GroupMembersTable.insert {
            it[GroupMembersTable.groupId] = EntityID(groupIdVal, GroupsTable)
            it[GroupMembersTable.userId] = EntityID(userIdVal, UsersTable)
            it[GroupMembersTable.role] = roleVal
        }
    }

    fun removeMember(groupIdVal: UUID, userIdVal: UUID) = transaction(db) {
        GroupMembersTable.deleteWhere {
            (groupId eq EntityID(groupIdVal, GroupsTable)) and
                (userId eq EntityID(userIdVal, UsersTable))
        }
    }

    fun findUserById(userId: UUID): ResultRow? = transaction(db) {
        UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull()
    }
}
