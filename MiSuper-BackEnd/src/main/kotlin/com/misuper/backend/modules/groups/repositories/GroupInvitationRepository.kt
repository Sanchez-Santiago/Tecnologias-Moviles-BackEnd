package com.misuper.backend.modules.groups.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.GroupInvitationsTable
import com.misuper.backend.database.tables.GroupsTable
import com.misuper.backend.database.tables.UsersTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime
import java.util.UUID

class GroupInvitationRepository {

    private val db get() = DatabaseFactory.getDatabase()

    fun create(groupId: UUID, invitedEmail: String, invitedBy: UUID): UUID = transaction(db) {
        GroupInvitationsTable.insert {
            it[GroupInvitationsTable.groupId] = EntityID(groupId, GroupsTable)
            it[GroupInvitationsTable.invitedEmail] = invitedEmail.lowercase()
            it[GroupInvitationsTable.invitedBy] = EntityID(invitedBy, UsersTable)
        }[GroupInvitationsTable.id].value
    }

    fun findByToken(token: String): ResultRow? = transaction(db) {
        GroupInvitationsTable.selectAll()
            .where { GroupInvitationsTable.token eq token }
            .singleOrNull()
    }

    fun findPendingByEmail(email: String): List<ResultRow> = transaction(db) {
        GroupInvitationsTable.selectAll()
            .where {
                GroupInvitationsTable.invitedEmail eq email.lowercase() and
                    (GroupInvitationsTable.status eq "PENDING")
            }
            .toList()
    }

    fun findByGroupAndEmail(groupId: UUID, email: String): ResultRow? = transaction(db) {
        GroupInvitationsTable.selectAll()
            .where {
                GroupInvitationsTable.groupId eq EntityID(groupId, GroupsTable) and
                    (GroupInvitationsTable.invitedEmail eq email.lowercase())
            }
            .toList()
            .firstOrNull { it[GroupInvitationsTable.status] == "PENDING" }
    }

    fun updateStatus(id: UUID, newStatus: String) = transaction(db) {
        GroupInvitationsTable.update({ GroupInvitationsTable.id eq id }) {
            it[GroupInvitationsTable.status] = newStatus
            it[GroupInvitationsTable.updatedAt] = LocalDateTime.now()
        }
    }
}
