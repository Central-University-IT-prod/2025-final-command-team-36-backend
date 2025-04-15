package de.teamnoco.books.service

import de.teamnoco.books.data.attachment.repo.AttachmentRepository
import de.teamnoco.books.data.book.dao.BookEntity
import de.teamnoco.books.data.book.dao.BookEntity.Companion.asEntity
import de.teamnoco.books.data.book.dao.BookEntity.Companion.asModel
import de.teamnoco.books.data.book.dao.BookFavoriteEntity
import de.teamnoco.books.data.book.dto.BookCreateRequest
import de.teamnoco.books.data.book.dto.BookModifyRequest
import de.teamnoco.books.data.book.exception.BookModerationNotNeededException
import de.teamnoco.books.data.book.model.Book
import de.teamnoco.books.data.book.repo.BookFavoriteRepository
import de.teamnoco.books.data.book.repo.BookRepository
import de.teamnoco.books.data.instance.repository.BookInstanceRepository
import de.teamnoco.books.data.transaction.repository.TransactionRepository
import de.teamnoco.books.data.user.enum.UserRole
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.web.exception.base.NotFoundException
import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val bookFavoriteRepository: BookFavoriteRepository,
    private val attachmentRepository: AttachmentRepository,
    private val bookInstanceRepository: BookInstanceRepository,
    private val transactionRepository: TransactionRepository,
    private val jdbcTemplate: JdbcTemplate
) {

    @PostConstruct
    fun init() {
        jdbcTemplate.execute("SET SCHEMA 'public'; CREATE EXTENSION IF NOT EXISTS pg_trgm")
    }

    // TODO
    fun getAll() = bookRepository.findAll().map { it.asModel() }

    fun create(user: User, request: BookCreateRequest): Book {
        val status = if (user.role == UserRole.ADMIN) {
            Book.Status.ACTIVE
        } else {
            Book.Status.MODERATION
        }

        attachmentRepository.findById(request.coverId).orElseThrow { NotFoundException("Attachment not found") }

        return bookRepository.save(
            BookEntity(
                name = request.name,
                author = request.author,
                isbn = request.isbn,
                genre = request.genre,
                editionYear = request.editionYear,
                publishingCompany = request.publishingCompany,
                language = request.language,
                cover = request.cover,
                pages = request.pages,
                size = request.size,
                coverId = request.coverId,
                status = status
            )
        ).asModel()
    }

    fun modify(id: UUID, request: BookModifyRequest): Book {
        val book = getById(id)
        if (request.coverId != null) {
            attachmentRepository.findById(request.coverId).orElseThrow { NotFoundException("Attachment not found") }
        }

        val newBook = book.copy(
            name = request.name ?: book.name,
            author = request.author ?: book.author,
            isbn = request.isbn ?: book.isbn,
            genre = request.genre ?: book.genre,
            editionYear = request.editionYear ?: book.editionYear,
            publishingCompany = request.publishingCompany ?: book.publishingCompany,
            language = request.language ?: book.language,
            cover = request.cover ?: book.cover,
            pages = request.pages ?: book.pages,
            size = request.size ?: book.size,
            coverId = request.coverId ?: book.coverId
        )

        return bookRepository.save(newBook.asEntity()).asModel()
    }

    @Transactional
    fun delete(id: UUID) {
        getById(id)

        val instances = bookInstanceRepository.findInstancesByBookId(id)
        instances.forEach {
            transactionRepository.deleteAllByInstanceId(it.id!!)
        }

        bookInstanceRepository.deleteAllByBookId(id)
        bookRepository.deleteById(id)
    }

    fun search(searchQuery: String): List<Book> {
        val pageable = PageRequest.of(0, 10)

        return bookRepository.search(searchQuery.trim(), pageable).map { it.asModel() }
    }

    fun getById(id: UUID): Book =
        bookRepository.findById(id).orElseThrow { NotFoundException("Book not found") }.asModel()

    fun favorite(bookId: UUID, user: User) {
        val book = getById(bookId)
        if (book.status != Book.Status.ACTIVE) {
            throw NotFoundException("Book not found")
        }

        bookFavoriteRepository.save(
            BookFavoriteEntity(
                id = BookFavoriteEntity.Id(bookId = book.id, userId = user.id!!),
            )
        )
    }

    fun unfavorite(bookId: UUID, user: User) {
        val book = getById(bookId)
        if (book.status != Book.Status.ACTIVE) {
            throw NotFoundException("Book not found")
        }

        bookFavoriteRepository.deleteById(
            BookFavoriteEntity.Id(bookId = book.id, userId = user.id!!)
        )
    }

    fun getAllByIds(ids: Set<UUID>): Set<Book> {
        return bookRepository.findAllByIdInAndStatus(ids).mapTo(mutableSetOf()) { it.asModel() }
    }

    fun getUserFavorites(user: User): Set<Book> {
        val favorites = bookFavoriteRepository.findAllByUserId(user.id!!)

        return getAllByIds(favorites.mapTo(mutableSetOf()) { it.id.bookId })
    }

    fun approveBook(id: UUID): Book {
        val book = getById(id)
        if (book.status != Book.Status.MODERATION) {
            throw BookModerationNotNeededException()
        }

        return bookRepository.save(book.copy(status = Book.Status.ACTIVE).asEntity()).asModel()
    }

    @Transactional
    fun rejectBook(id: UUID) {
        val book = getById(id)
        if (book.status != Book.Status.MODERATION) {
            throw BookModerationNotNeededException()
        }

        delete(id)
    }

    fun getModerationList() = bookRepository.getModerationList().map { it.asModel() }

}
