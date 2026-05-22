package com.misuper.backend.modules.tickets.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.GroupsTable
import com.misuper.backend.database.tables.TicketMessagesTable
import com.misuper.backend.database.tables.TicketsTable
import com.misuper.backend.database.tables.UsersTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime
import java.util.UUID

class TicketRepository {

    private val db get() = DatabaseFactory.getDatabase()

    fun findById(id: UUID): ResultRow? = transaction(db) {
        TicketsTable.selectAll().where { TicketsTable.id eq id }.singleOrNull()
    }

    fun findByGroupId(groupIdVal: UUID): List<ResultRow> = transaction(db) {
        TicketsTable.selectAll()
            .where {
                (TicketsTable.groupId eq EntityID(groupIdVal, GroupsTable)) and
                    (TicketsTable.active eq true)
            }
            .orderBy(TicketsTable.createdAt, SortOrder.DESC_NULLS_LAST)
            .toList()
    }

    fun create(
        groupIdVal: UUID,
        createdByVal: UUID,
        titleVal: String,
        descriptionVal: String,
        priorityVal: String,
        assignedToVal: UUID?
    ): UUID = transaction(db) {
        TicketsTable.insert { stmt ->
            stmt[TicketsTable.groupId] = EntityID(groupIdVal, GroupsTable)
            stmt[TicketsTable.createdBy] = EntityID(createdByVal, UsersTable)
            stmt[TicketsTable.title] = titleVal
            stmt[TicketsTable.description] = descriptionVal
            stmt[TicketsTable.priority] = priorityVal
            if (assignedToVal != null) {
                stmt[TicketsTable.assignedTo] = EntityID(assignedToVal, UsersTable)
            }
        }[TicketsTable.id].value
    }

    fun update(
        id: UUID,
        titleVal: String?,
        descriptionVal: String?,
        statusVal: String?,
        priorityVal: String?,
        assignedToVal: UUID?
    ) = transaction(db) {
        TicketsTable.update({ TicketsTable.id eq id }) { stmt ->
            titleVal?.let { stmt[TicketsTable.title] = it }
            descriptionVal?.let { stmt[TicketsTable.description] = it }
            statusVal?.let { stmt[TicketsTable.status] = it }
            priorityVal?.let { stmt[TicketsTable.priority] = it }
            if (assignedToVal != null) stmt[TicketsTable.assignedTo] = EntityID(assignedToVal, UsersTable)
            stmt[TicketsTable.updatedAt] = LocalDateTime.now()
        }
    }

    fun softDelete(id: UUID) = transaction(db) {
        TicketsTable.update({ TicketsTable.id eq id }) { stmt ->
            stmt[TicketsTable.active] = false
            stmt[TicketsTable.updatedAt] = LocalDateTime.now()
        }
    }

    fun getMessages(ticketIdVal: UUID): List<ResultRow> = transaction(db) {
        TicketMessagesTable.selectAll()
            .where { TicketMessagesTable.ticketId eq EntityID(ticketIdVal, TicketsTable) }
            .orderBy(TicketMessagesTable.createdAt, SortOrder.ASC_NULLS_LAST)
            .toList()
    }

    fun addMessage(ticketIdVal: UUID, userIdVal: UUID, messageVal: String) = transaction(db) {
        TicketMessagesTable.insert { stmt ->
            stmt[TicketMessagesTable.ticketId] = EntityID(ticketIdVal, TicketsTable)
            stmt[TicketMessagesTable.userId] = EntityID(userIdVal, UsersTable)
            stmt[TicketMessagesTable.message] = messageVal
        }
    }

    fun findUserById(id: UUID): ResultRow? = transaction(db) {
        UsersTable.selectAll().where { UsersTable.id eq id }.singleOrNull()
    }
}
