package de.teamnoco.books.test.unit

import de.teamnoco.books.service.BookInstanceService
import de.teamnoco.books.service.BookService
import de.teamnoco.books.service.BookTrendService
import de.teamnoco.books.service.TransactionService
import de.teamnoco.books.test.util.ModelGenerators.getRandomBook
import de.teamnoco.books.test.util.ModelGenerators.getRandomBookInstance
import de.teamnoco.books.test.util.ModelGenerators.getRandomTransaction
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
class BookTrendServiceTest : StringSpec({

    val transactionService = mockk<TransactionService>()
    val bookService = mockk<BookService>()
    val bookInstanceService = mockk<BookInstanceService>()

    val bookTrendService = BookTrendService(transactionService, bookService, bookInstanceService)

    "should get trending books" {
        val transactions = (1..50).map { getRandomTransaction() }
        val bookInstances = transactions.map { getRandomBookInstance().copy(id = it.instanceId) }.toSet()
        val books = bookInstances.map { getRandomBook().copy(id = it.bookId) }.toSet()

        every { transactionService.getTransactionsByType(any(), any<LocalDateTime>()) } returns transactions
        every { bookInstanceService.getAllByIds(any(), any()) } returns bookInstances
        every { bookService.getAllByIds(any()) } returns books

        val result = bookTrendService.getBooksWithBorrowTransactions(LocalDateTime.now().minusDays(7))

        result.size shouldBe 50
        result.values.toList() shouldBe result.values.sortedByDescending { it }
    }

})