package de.teamnoco.books.service

import de.teamnoco.books.data.book.model.Book
import de.teamnoco.books.data.instance.model.BookInstance
import de.teamnoco.books.data.transaction.model.Transaction
import de.teamnoco.books.data.user.model.User
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import kotlin.math.max
import kotlin.math.min

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
@Service
class BookRecommendationService(
    private val bookInstanceService: BookInstanceService,
    private val bookService: BookService,
    private val transactionService: TransactionService
) {

    @Cacheable("books-scored")
    fun getBooksScoredForUser(user: User): Map<Book, Int> {
        val booksInStock = bookInstanceService.getIdsInStock()
        val books = bookService.getAllByIds(booksInStock) - bookService.getUserFavorites(user).toSet()

        val borrowedBookInstanceIds = transactionService.getTransactionsByType(Transaction.Type.BORROW, user.id!!)
            .map { it.instanceId }.toSet()
        val borrowedBookIds =
            bookInstanceService.getAllByIds(borrowedBookInstanceIds, listOf(BookInstance.Status.PLACED))
                .map { it.bookId }.toSet()
        val borrowedBooks = bookService.getAllByIds(borrowedBookIds)

        val genreFrequency = getFrequency(borrowedBooks) { genre }
        val authorFrequency = getFrequency(borrowedBooks) { author }
        val hardCoverFrequency = getFrequency(borrowedBooks) { cover.name }
        val sizeFrequency = getFrequency(borrowedBooks) { size.toString() }
        // TODO: мб добавить кол-во страниц

        val scoredBooks = books.associateWith {
            getScoreForBook(
                it, user, genreFrequency, authorFrequency, hardCoverFrequency, sizeFrequency
            )
        }.normalize()

        return scoredBooks.mapValues { (_, score) -> (score * 100).toInt() }.toList().sortedByDescending { it.second }
            .toMap()
    }

    fun getFrequency(books: Set<Book>, supplier: Book.() -> String): Map<String, Float> {
        val total = books.size.toFloat()
        if (total == 0f) return emptyMap()

        return books.groupingBy { supplier(it) }.eachCount().mapValues { (_, count) -> count.toFloat() / total }
    }

    fun getScoreForBook(
        book: Book,
        user: User,
        genreFrequency: Map<String, Float>,
        authorFrequency: Map<String, Float>,
        hardCoverFrequency: Map<String, Float>,
        sizeFrequency: Map<String, Float>
    ): Float {
        return genreFrequency.getOrDefault(book.genre, 0f) + authorFrequency.getOrDefault(
            book.author, 0f
        ) + hardCoverFrequency.getOrDefault(
            book.cover.name, 0f
        ) + sizeFrequency.getOrDefault(book.size.toString(), 0f)
    }

    private fun <K, V : Number> Map<K, V>.normalize(min: Float? = null, max: Float? = null): Map<K, Float> {
        if (values.toSet().size == 1) return mapValues { 0f }

        val floatValues = mapValues { it.value.toFloat() }

        val minValue = min ?: floatValues.values.minOrNull() ?: 0f
        val maxValue = max ?: floatValues.values.maxOrNull() ?: 1f

        return floatValues.mapValues { min(max((it.value - minValue) / (maxValue - minValue), 0f), 1f) }
    }

}