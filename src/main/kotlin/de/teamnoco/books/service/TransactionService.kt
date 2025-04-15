package de.teamnoco.books.service

import de.teamnoco.books.data.transaction.dao.TransactionEntity.Companion.asModel
import de.teamnoco.books.data.transaction.model.Transaction
import de.teamnoco.books.data.transaction.repository.TransactionRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
@Service
class TransactionService(
    private val transactionRepository: TransactionRepository
) {

    fun getTransactionsByType(type: Transaction.Type, since: LocalDateTime): List<Transaction> {
        return transactionRepository.findAllByTypeAndCreatedAtAfter(type, since).map { it.asModel() }
    }

    fun getTransactionsByType(type: Transaction.Type, userId: UUID): List<Transaction> {
        return transactionRepository.findAllByTypeAndUserId(type, userId).map { it.asModel() }
    }

}