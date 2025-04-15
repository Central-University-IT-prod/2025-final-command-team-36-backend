package de.teamnoco.books.test.unit

import de.teamnoco.books.data.transaction.dao.TransactionEntity.Companion.asEntity
import de.teamnoco.books.data.transaction.repository.TransactionRepository
import de.teamnoco.books.service.TransactionService
import de.teamnoco.books.test.util.ModelGenerators.getRandomTransaction
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
class TransactionServiceTest : StringSpec({

    val transactionRepository = mockk<TransactionRepository>()
    val transactionService = TransactionService(transactionRepository)

    "should get by type and since" {
        val transactions = (1..50).map { getRandomTransaction() }

        val type = transactions.first().type
        val expectedTransactions = transactions.filter { it.type == type }

        every { transactionRepository.findAllByTypeAndCreatedAtAfter(any(), any()) } answers {
            transactions.filter { it.type == firstArg() && it.createdAt.isAfter(secondArg()) }.map { it.asEntity() }
        }

        val result = transactionService.getTransactionsByType(type, LocalDateTime.now().minusDays(1))
        result shouldBe expectedTransactions
    }

    "should get by type and user id" {
        val transactions = (1..50).map { getRandomTransaction() }

        val type = transactions.first().type
        val userId = transactions.first().userId
        val expectedTransactions = transactions.filter { it.type == type && it.userId == userId }

        every { transactionRepository.findAllByTypeAndUserId(any(), any()) } answers {
            transactions.filter { it.type == firstArg() && it.userId == secondArg() }.map { it.asEntity() }
        }

        val result = transactionService.getTransactionsByType(type, userId)
        result shouldBe expectedTransactions
    }

})