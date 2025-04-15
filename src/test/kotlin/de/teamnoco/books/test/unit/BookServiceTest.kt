package de.teamnoco.books.test.unit

import de.teamnoco.books.data.attachment.dao.AttachmentEntity.Companion.asEntity
import de.teamnoco.books.data.attachment.repo.AttachmentRepository
import de.teamnoco.books.data.book.dao.BookEntity
import de.teamnoco.books.data.book.dao.BookEntity.Companion.asEntity
import de.teamnoco.books.data.book.dao.BookFavoriteEntity
import de.teamnoco.books.data.book.exception.BookModerationNotNeededException
import de.teamnoco.books.data.book.model.Book
import de.teamnoco.books.data.book.repo.BookFavoriteRepository
import de.teamnoco.books.data.book.repo.BookRepository
import de.teamnoco.books.data.instance.repository.BookInstanceRepository
import de.teamnoco.books.data.transaction.repository.TransactionRepository
import de.teamnoco.books.data.user.enum.UserRole
import de.teamnoco.books.service.BookService
import de.teamnoco.books.test.util.ModelGenerators.getRandomAttachment
import de.teamnoco.books.test.util.ModelGenerators.getRandomBook
import de.teamnoco.books.test.util.ModelGenerators.getRandomBookCreateRequest
import de.teamnoco.books.test.util.ModelGenerators.getRandomBookModifyRequest
import de.teamnoco.books.test.util.ModelGenerators.getRandomUser
import de.teamnoco.books.web.exception.base.NotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.jdbc.core.JdbcTemplate
import java.util.*

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
class BookServiceTest : StringSpec({

    val bookRepository = mockk<BookRepository>()
    val bookFavoriteRepository = mockk<BookFavoriteRepository>()
    val attachmentRepository = mockk<AttachmentRepository>()
    val bookInstanceRepository = mockk<BookInstanceRepository>()
    val transactionRepository = mockk<TransactionRepository>()
    val jdbcTemplate = mockk<JdbcTemplate>()

    every { jdbcTemplate.execute(any()) } just Runs

    val bookService = BookService(
        bookRepository,
        bookFavoriteRepository,
        attachmentRepository,
        bookInstanceRepository,
        transactionRepository,
        jdbcTemplate
    )

    "should get all books" {
        val books = (1..50).map { getRandomBook() }

        every { bookRepository.findAll() } returns books.map { it.asEntity() }

        val result = bookService.getAll()

        result shouldBe books
    }

    "should get by id" {
        val book = getRandomBook()

        every { bookRepository.findById(any()) } returns Optional.of(book.asEntity())

        val result = bookService.getById(book.id)

        result shouldBe book
    }

    "should throw NotFoundException if book not found by id" {
        every { bookRepository.findById(any()) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            bookService.getById(UUID.randomUUID())
        }
    }

    "should get by ids" {
        val books = (1..50).map { getRandomBook() }

        every { bookRepository.findAllByIdInAndStatus(any(), any()) } returns books.map { it.asEntity() }

        val result = bookService.getAllByIds(books.map { it.id }.toSet())

        result shouldBe books
    }

    "should create book" {
        val user = getRandomUser()
        val request = getRandomBookCreateRequest()
        val expectedBook = Book(
            id = UUID.randomUUID(),
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
            status = Book.Status.MODERATION
        )

        every { attachmentRepository.findById(request.coverId) } returns Optional.of(getRandomAttachment().asEntity())
        every { bookRepository.save(any()) } answers { firstArg<BookEntity>().copy(id = expectedBook.id) }

        val result = bookService.create(user, request)

        result shouldBe expectedBook

        verify { bookRepository.save(any()) }
    }

    "should skip moderation for ADMIN when creating" {
        val user = getRandomUser().copy(role = UserRole.ADMIN)
        val request = getRandomBookCreateRequest()
        val expectedBook = Book(
            id = UUID.randomUUID(),
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
            status = Book.Status.ACTIVE
        )

        every { attachmentRepository.findById(request.coverId) } returns Optional.of(getRandomAttachment().asEntity())
        every { bookRepository.save(any()) } answers { firstArg<BookEntity>().copy(id = expectedBook.id) }

        val result = bookService.create(user, request)

        result shouldBe expectedBook

        verify { bookRepository.save(any()) }
    }

    "should throw NotFoundException if attachment not found" {
        val user = getRandomUser()
        val request = getRandomBookCreateRequest()

        every { attachmentRepository.findById(request.coverId) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            bookService.create(user, request)
        }
    }

    "should modify book" {
        val book = getRandomBook()
        val request = getRandomBookModifyRequest()

        every { attachmentRepository.findById(request.coverId!!) } returns Optional.of(getRandomAttachment().asEntity())
        every { bookRepository.findById(book.id) } returns Optional.of(book.asEntity())
        every { bookRepository.save(any()) } answers { firstArg<BookEntity>() }

        val result = bookService.modify(book.id, request)

        result shouldBe book.copy(
            name = request.name!!,
            author = request.author!!,
            isbn = request.isbn!!,
            genre = request.genre!!,
            editionYear = request.editionYear!!,
            publishingCompany = request.publishingCompany!!,
            language = request.language!!,
            cover = request.cover!!,
            pages = request.pages!!,
            size = request.size!!,
            coverId = request.coverId!!,
        )

        verify { bookRepository.save(any()) }
        verify { attachmentRepository.findById(request.coverId!!) }
    }

    "should not check attachment if not passed" {
        val book = getRandomBook()
        val request = getRandomBookModifyRequest().copy(coverId = null)

        every { bookRepository.findById(book.id) } returns Optional.of(book.asEntity())
        every { bookRepository.save(any()) } answers { firstArg<BookEntity>() }

        val result = bookService.modify(book.id, request)

        result shouldBe book.copy(
            name = request.name!!,
            author = request.author!!,
            isbn = request.isbn!!,
            genre = request.genre!!,
            editionYear = request.editionYear!!,
            publishingCompany = request.publishingCompany!!,
            language = request.language!!,
            cover = request.cover!!,
            pages = request.pages!!,
            size = request.size!!,
            coverId = book.coverId,
        )

        verify { bookRepository.save(any()) }
        verify(exactly = 0) { attachmentRepository.findById(book.coverId) }
    }

    "should throw NotFoundException if attachment not found and trying to modify" {
        val book = getRandomBook()
        val request = getRandomBookModifyRequest()

        every { attachmentRepository.findById(request.coverId!!) } returns Optional.empty()
        every { bookRepository.findById(book.id) } returns Optional.of(book.asEntity())

        shouldThrow<NotFoundException> {
            bookService.modify(book.id, request)
        }
    }

    "should delete book" {
        val book = getRandomBook()

        every { bookRepository.findById(book.id) } returns Optional.of(book.asEntity())
        every { bookInstanceRepository.findInstancesByBookId(book.id) } returns emptyList()
        every { bookRepository.deleteById(book.id) } just Runs
        every { bookInstanceRepository.deleteAllByBookId(any()) } just Runs

        bookService.delete(book.id)

        verify { bookRepository.deleteById(book.id) }
    }

    "should throw NotFoundException if book not found by id and trying to delete" {
        every { bookRepository.findById(any()) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            bookService.delete(UUID.randomUUID())
        }
    }

    "should search books" {
        val books = (1..50).map { getRandomBook() }
        val searchQuery = "Александр Шахов Евгений Кабарухин"

        every { bookRepository.search(searchQuery, any()) } returns books.map { it.asEntity() }

        val result = bookService.search(searchQuery)

        result shouldBe books
    }

    "should favorite book" {
        val user = getRandomUser()
        val book = getRandomBook()

        every { bookRepository.findById(book.id) } returns Optional.of(book.asEntity())
        every { bookFavoriteRepository.save(any()) } answers { firstArg() }

        bookService.favorite(book.id, user)

        verify { bookFavoriteRepository.save(any()) }
    }

    "should not favorite unmoderated book" {
        val user = getRandomUser()
        val book = getRandomBook().copy(status = Book.Status.MODERATION)

        every { bookRepository.findById(book.id) } returns Optional.of(book.asEntity())

        shouldThrow<NotFoundException> {
            bookService.favorite(book.id, user)
        }
    }

    "should unfavorite book" {
        val user = getRandomUser()
        val book = getRandomBook()

        every { bookRepository.findById(book.id) } returns Optional.of(book.asEntity())
        every { bookFavoriteRepository.deleteById(any()) } just Runs

        bookService.unfavorite(book.id, user)

        verify { bookFavoriteRepository.deleteById(BookFavoriteEntity.Id(bookId = book.id, userId = user.id!!)) }
    }

    "should not unfavorite unmoderated book" {
        val user = getRandomUser()
        val book = getRandomBook().copy(status = Book.Status.MODERATION)

        every { bookRepository.findById(book.id) } returns Optional.of(book.asEntity())

        shouldThrow<NotFoundException> {
            bookService.unfavorite(book.id, user)
        }
    }

    "should get user favorites" {
        val user = getRandomUser()
        val books = (1..50).map { getRandomBook() }
        val favorites = books.map { BookFavoriteEntity(id = BookFavoriteEntity.Id(bookId = it.id, userId = user.id!!)) }

        every { bookFavoriteRepository.findAllByUserId(user.id!!) } returns favorites
        every { bookRepository.findAllByIdInAndStatus(books.map { it.id }.toSet()) } returns books.map { it.asEntity() }

        val result = bookService.getUserFavorites(user)

        result shouldContainAll books
    }

    "should approve book" {
        val book = getRandomBook().copy(status = Book.Status.MODERATION)

        every { bookRepository.findById(book.id) } returns Optional.of(book.asEntity())
        every { bookRepository.save(any()) } answers { firstArg() }

        val result = bookService.approveBook(book.id)

        result shouldBe book.copy(status = Book.Status.ACTIVE)

        verify { bookRepository.save(any()) }
    }

    "should throw BookModerationNotNeededException if book is already active and tries to approve" {
        val book = getRandomBook().copy(status = Book.Status.ACTIVE)

        every { bookRepository.findById(book.id) } returns Optional.of(book.asEntity())

        shouldThrow<BookModerationNotNeededException> {
            bookService.approveBook(book.id)
        }
    }

    "should reject book" {
        val book = getRandomBook().copy(status = Book.Status.MODERATION)

        every { bookRepository.findById(book.id) } returns Optional.of(book.asEntity())
        every { bookRepository.deleteById(book.id) } just Runs
        every { bookInstanceRepository.findInstancesByBookId(any()) } returns emptyList()

        bookService.rejectBook(book.id)

        verify { bookRepository.deleteById(book.id) }
    }

    "should throw BookModerationNotNeededException if book is already active and tries to reject" {
        val book = getRandomBook().copy(status = Book.Status.ACTIVE)

        every { bookRepository.findById(book.id) } returns Optional.of(book.asEntity())

        shouldThrow<BookModerationNotNeededException> {
            bookService.rejectBook(book.id)
        }
    }

    "should get moderation list" {
        val books = (1..50).map { getRandomBook() }

        every { bookRepository.getModerationList() } returns books.map { it.asEntity() }

        val result = bookService.getModerationList()

        result shouldBe books
    }

})