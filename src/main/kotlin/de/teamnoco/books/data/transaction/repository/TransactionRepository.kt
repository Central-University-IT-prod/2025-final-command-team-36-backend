package de.teamnoco.books.data.transaction.repository

import de.teamnoco.books.data.transaction.dao.TransactionEntity
import de.teamnoco.books.data.transaction.model.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.*

interface TransactionRepository : JpaRepository<TransactionEntity, UUID> {
    fun findAllByTypeAndCreatedAtAfter(type: Transaction.Type, since: LocalDateTime): List<TransactionEntity>

    fun findAllByTypeAndUserId(type: Transaction.Type, userId: UUID): List<TransactionEntity>

    fun deleteAllByInstanceId(instanceId: UUID)

    fun deleteAllByInstanceIdIn(instanceIds: List<UUID>)

}