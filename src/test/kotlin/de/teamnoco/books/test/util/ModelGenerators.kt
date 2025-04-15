package de.teamnoco.books.test.util

import de.teamnoco.books.data.attachment.model.Attachment
import de.teamnoco.books.data.book.dto.BookCreateRequest
import de.teamnoco.books.data.book.dto.BookModifyRequest
import de.teamnoco.books.data.book.enum.BookSize
import de.teamnoco.books.data.book.model.Book
import de.teamnoco.books.data.instance.dao.BookInstanceEntity.Companion.asEntity
import de.teamnoco.books.data.instance.dto.BookInstanceCreateRequest
import de.teamnoco.books.data.instance.dto.BookInstanceModifyRequest
import de.teamnoco.books.data.instance.model.BookInstance
import de.teamnoco.books.data.location.dto.LocationCreateRequest
import de.teamnoco.books.data.location.dto.LocationModifyRequest
import de.teamnoco.books.data.location.model.Location
import de.teamnoco.books.data.reservation.dao.ReservationEntity
import de.teamnoco.books.data.transaction.model.Transaction
import de.teamnoco.books.data.user.enum.UserRole
import de.teamnoco.books.data.user.model.User
import java.time.LocalDateTime
import java.util.*

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
object ModelGenerators {

    fun getRandomUser() = User(
        id = randomUUID(),
        name = randomString(),
        email = randomString() + "@gmail.com",
        password = randomString(),
        role = UserRole.USER
    )

    fun getRandomTransaction() = Transaction(
        id = randomUUID(),
        type = Transaction.Type.entries.toTypedArray().random(),
        instanceId = randomUUID(),
        userId = randomUUID(),
        createdAt = LocalDateTime.now()
    )

    fun getRandomBook() = Book(
        id = randomUUID(),
        name = randomString(),
        author = randomString(),
        isbn = randomString(),
        publishingCompany = randomString(),
        editionYear = randomInt(2000, 2025),
        pages = randomInt(),
        language = randomString(),
        cover = Book.Cover.entries.toTypedArray().random(),
        genre = randomString(),
        size = BookSize.entries.toTypedArray().random(),
        coverId = randomUUID(),
        status = Book.Status.ACTIVE
    )

    fun getRandomBookInstance() = BookInstance(
        id = randomUUID(),
        bookId = randomUUID(),
        description = randomString(),
        status = BookInstance.Status.entries.toTypedArray().random(),
        createdAt = LocalDateTime.now(),
        condition = BookInstance.Condition.entries.toTypedArray().random(),
        owner = getRandomUser(),
        photoId = randomUUID(),
        location = getRandomLocation()
    )

    fun getRandomLocationCreateRequest() = LocationCreateRequest(
        name = randomString(),
        address = randomString(),
        extra = randomString(),
        limit = randomInt()
    )

    fun getRandomLocationModifyRequest() = LocationModifyRequest(
        name = randomString(),
        extra = randomString(),
        limit = randomInt()
    )

    fun getRandomLocation() = Location(
        id = randomUUID(),
        name = randomString(),
        address = randomString(),
        extra = randomString(),
        limit = randomInt()
    )

    fun getRandomReservationEntity() = ReservationEntity(
        id = randomUUID(),
        instance = getRandomBookInstance().asEntity(),
        userId = randomUUID(),
        createdAt = LocalDateTime.now()
    )

    fun getRandomBookCreateRequest() = BookCreateRequest(
        name = randomString(),
        author = randomString(),
        isbn = randomString(),
        genre = randomString(),
        editionYear = randomInt(),
        publishingCompany = randomString(),
        language = randomString(),
        cover = Book.Cover.entries.toTypedArray().random(),
        pages = randomInt(),
        size = BookSize.entries.toTypedArray().random(),
        coverId = randomUUID()
    )

    fun getRandomBookModifyRequest() = BookModifyRequest(
        name = randomString(),
        author = randomString(),
        isbn = randomString(),
        genre = randomString(),
        editionYear = randomInt(),
        publishingCompany = randomString(),
        language = randomString(),
        cover = Book.Cover.entries.toTypedArray().random(),
        pages = randomInt(),
        size = BookSize.entries.toTypedArray().random(),
        coverId = randomUUID()
    )

    fun getRandomAttachment() = Attachment(
        id = randomUUID(),
        extension = randomString(),
        contentType = randomString()
    )

    fun getRandomBookInstanceCreateRequest() = BookInstanceCreateRequest(
        description = randomString(),
        condition = BookInstance.Condition.entries.toTypedArray().random(),
        photoId = randomUUID(),
        locationId = randomUUID(),
        bookId = randomUUID()
    )

    fun getRandomBookInstanceModifyRequest() = BookInstanceModifyRequest(
        description = randomString(),
        condition = BookInstance.Condition.entries.toTypedArray().random(),
        photoId = randomUUID(),
        locationId = randomUUID(),
        status = BookInstance.Status.entries.toTypedArray().random()
    )

    private fun randomUUID() = UUID.randomUUID()

    private fun randomString(length: Int = 8) = (1..length).map { ('a'..'z').random() }.joinToString("")

    private fun randomInt(a: Int = 1, b: Int = 100) = (a..b).random()

    private fun randomFloat() = randomInt().toFloat()

}