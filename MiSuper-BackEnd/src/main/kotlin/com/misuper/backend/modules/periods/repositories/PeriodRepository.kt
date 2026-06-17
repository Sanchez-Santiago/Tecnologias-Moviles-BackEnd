package com.misuper.backend.modules.periods.repositories

import com.misuper.backend.database.DatabaseFactory
import com.misuper.backend.database.tables.GroupMonthlyPeriodsTable
import com.misuper.backend.database.tables.GroupsTable
import com.misuper.backend.database.tables.UsersTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class PeriodRepository {

    private val db get() = DatabaseFactory.getDatabase()

    fun findById(id: UUID): ResultRow? = transaction(db) {
        GroupMonthlyPeriodsTable.selectAll()
            .where { GroupMonthlyPeriodsTable.id eq id }
            .singleOrNull()
    }

    fun findByGroupAndStatus(groupId: UUID, status: String): ResultRow? = transaction(db) {
        GroupMonthlyPeriodsTable.selectAll()
            .where {
                (GroupMonthlyPeriodsTable.groupId eq EntityID(groupId, GroupsTable)) and
                    (GroupMonthlyPeriodsTable.status eq status)
            }
            .singleOrNull()
    }

    fun findByGroup(groupId: UUID): List<ResultRow> = transaction(db) {
        GroupMonthlyPeriodsTable.selectAll()
            .where { GroupMonthlyPeriodsTable.groupId eq EntityID(groupId, GroupsTable) }
            .orderBy(GroupMonthlyPeriodsTable.createdAt, org.jetbrains.exposed.v1.core.SortOrder.DESC)
            .toList()
    }

    fun create(
        groupId: UUID,
        name: String?,
        startDate: LocalDateTime,
        cycleType: String
    ): UUID = transaction(db) {
        GroupMonthlyPeriodsTable.insert {
            it[GroupMonthlyPeriodsTable.groupId] = EntityID(groupId, GroupsTable)
            it[GroupMonthlyPeriodsTable.name] = name
            it[GroupMonthlyPeriodsTable.startDate] = startDate
            it[GroupMonthlyPeriodsTable.status] = "OPEN"
            it[GroupMonthlyPeriodsTable.cycleType] = cycleType
        }[GroupMonthlyPeriodsTable.id].value
    }

    fun close(
        periodId: UUID,
        closedByUserId: UUID,
        finalBalance: BigDecimal?
    ) = transaction(db) {
        GroupMonthlyPeriodsTable.update({ GroupMonthlyPeriodsTable.id eq periodId }) {
            it[status] = "CLOSED"
            it[closedBy] = EntityID(closedByUserId, UsersTable)
            it[endDate] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
            if (finalBalance != null) {
                it[GroupMonthlyPeriodsTable.finalBalance] = finalBalance
            }
        }
    }
}
