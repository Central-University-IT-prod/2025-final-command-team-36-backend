package de.teamnoco.books.test.unit

import de.teamnoco.books.service.BookInstanceService
import de.teamnoco.books.service.BookRecommendationService
import de.teamnoco.books.service.BookService
import de.teamnoco.books.service.TransactionService
import de.teamnoco.books.test.util.ModelGenerators.getRandomBook
import de.teamnoco.books.test.util.ModelGenerators.getRandomBookInstance
import de.teamnoco.books.test.util.ModelGenerators.getRandomUser
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.util.*

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
class BookRecommendationServiceTest : StringSpec({

    val bookService = mockk<BookService>()
    val bookInstanceService = mockk<BookInstanceService>()
    val transactionService = mockk<TransactionService>()

    val bookRepositoryService = BookRecommendationService(bookInstanceService, bookService, transactionService)

    "should recommend books" {
        val books = (1..50).map { getRandomBook() }.toSet()
        val bookInstances = books.map { getRandomBookInstance().copy(bookId = it.id) }.toSet()

        every { transactionService.getTransactionsByType(any(), any<UUID>()) } returns emptyList()
        every { bookInstanceService.getAllByIds(any(), any()) } returns bookInstances

        every { bookInstanceService.getIdsInStock() } returns bookInstances.map { it.id }.toSet()
        every { bookService.getAllByIds(any()) } returns books
        every { bookService.getUserFavorites(any()) } returns emptySet()

        val result = bookRepositoryService.getBooksScoredForUser(getRandomUser())

        result.size shouldBe 50
        result.values.toList() shouldBe result.values.sortedByDescending { it }
    }

})