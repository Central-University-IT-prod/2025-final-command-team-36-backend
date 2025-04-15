package de.teamnoco.books.service

import de.teamnoco.books.data.book.model.Book
import de.teamnoco.books.data.instance.model.BookInstance
import de.teamnoco.books.data.transaction.model.Transaction
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
@Service
class BookTrendService(
    private val transactionService: TransactionService,
    private val bookService: BookService,
    private val bookInstanceService: BookInstanceService
) {

    @Cacheable("books-trends")
    fun getBooksWithBorrowTransactions(since: LocalDateTime): Map<Book, Int> {
        val booksReceivedLastWeek = transactionService.getTransactionsByType(Transaction.Type.BORROW, since)
        val instances = booksReceivedLastWeek.map { it.instanceId }.toSet()

        val instanceObjects = bookInstanceService.getAllByIds(
            instances,
            listOf(
                BookInstance.Status.RESERVED,
                BookInstance.Status.PLACED,
                BookInstance.Status.REPORTED,
                BookInstance.Status.MODERATION,
                BookInstance.Status.RECEIVED
            )
        )
        val books = bookService.getAllByIds(instanceObjects.map { it.bookId }.toSet())

        val countedBooks = booksReceivedLastWeek.groupingBy { it.instanceId }.eachCount()
            .mapKeys {
                val instance = instanceObjects.firstOrNull { instance -> instance.id == it.key } ?: return@mapKeys null
                books.firstOrNull { book -> book.id == instance.bookId }
            }
            .filterKeys { it != null }.mapKeys { it.key!! }

        return countedBooks.toList().sortedByDescending { it.second }.toMap()
    }

}