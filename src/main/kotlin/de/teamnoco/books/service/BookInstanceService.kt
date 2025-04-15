package de.teamnoco.books.service

import de.teamnoco.books.data.attachment.repo.AttachmentRepository
import de.teamnoco.books.data.book.exception.BookNotFoundException
import de.teamnoco.books.data.book.repo.BookRepository
import de.teamnoco.books.data.instance.dao.BookInstanceEntity
import de.teamnoco.books.data.instance.dao.BookInstanceEntity.Companion.asEntity
import de.teamnoco.books.data.instance.dao.BookInstanceEntity.Companion.asModel
import de.teamnoco.books.data.instance.dto.BookInstanceCreateRequest
import de.teamnoco.books.data.instance.dto.BookInstanceModifyRequest
import de.teamnoco.books.data.instance.exception.BookInstanceModerationNotNeeded
import de.teamnoco.books.data.instance.model.BookInstance
import de.teamnoco.books.data.instance.repository.BookInstanceRepository
import de.teamnoco.books.data.location.dao.LocationEntity
import de.teamnoco.books.data.location.dao.LocationEntity.Companion.asEntity
import de.teamnoco.books.data.location.repository.LocationRepository
import de.teamnoco.books.data.report.repository.ReportRepository
import de.teamnoco.books.data.reservation.repository.ReservationRepository
import de.teamnoco.books.data.transaction.dao.TransactionEntity
import de.teamnoco.books.data.transaction.model.Transaction
import de.teamnoco.books.data.transaction.repository.TransactionRepository
import de.teamnoco.books.data.user.dao.UserEntity.Companion.asEntity
import de.teamnoco.books.data.user.enum.UserRole
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.web.exception.base.AccessDeniedException
import de.teamnoco.books.web.exception.base.NotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class BookInstanceService(
    private val bookRepo: BookRepository,
    private val bookInstanceRepository: BookInstanceRepository,
    private val attachmentRepo: AttachmentRepository,
    private val locationRepository: LocationRepository,
    private val transactionRepository: TransactionRepository,
    private val reservationRepository: ReservationRepository,
    private val reportRepository: ReportRepository
) {

    fun getInstancesByBookId(bookId: UUID, user: User?): List<BookInstance> {
        bookRepo.findById(bookId).orElseThrow { BookNotFoundException() }
        return bookInstanceRepository.findInstancesByBookIdAndStatusIn(
            bookId,
            if (user?.role != UserRole.ADMIN) {
                listOf(BookInstance.Status.PLACED)
            } else {
                listOf(BookInstance.Status.PLACED, BookInstance.Status.RESERVED, BookInstance.Status.REPORTED)
            }
        ).map { it.asModel() }
    }

    fun createInstance(request: BookInstanceCreateRequest, user: User): BookInstance {
        bookRepo.findById(request.bookId).orElseThrow { BookNotFoundException() }
        attachmentRepo.findById(request.photoId).orElseThrow { NotFoundException("Photo not found") }

        val location =
            locationRepository.findById(request.locationId).orElseThrow { NotFoundException("Location not found") }

        val status = if (user.role == UserRole.ADMIN) {
            BookInstance.Status.PLACED
        } else {
            BookInstance.Status.MODERATION
        }

        val instance = bookInstanceRepository.save(
            BookInstanceEntity(
                id = null,
                request.bookId,
                request.condition,
                user.asEntity(),
                request.photoId,
                location,
                request.description,
                status,
                LocalDateTime.now()
            )
        ).asModel()

        if (status == BookInstance.Status.PLACED) {
            transactionRepository.save(
                TransactionEntity(
                    null,
                    Transaction.Type.LEND,
                    instance.id,
                    user.id!!,
                    instance.createdAt
                )
            )
        }

        return instance
    }

    fun approveInstance(id: UUID): BookInstance {
        val instance = getInstance(id)
        if (instance.status != BookInstance.Status.MODERATION) {
            throw BookInstanceModerationNotNeeded()
        }

        val newInstance =
            bookInstanceRepository.save(instance.copy(status = BookInstance.Status.PLACED).asEntity()).asModel()
        transactionRepository.save(
            TransactionEntity(
                null,
                Transaction.Type.LEND,
                instance.id,
                instance.owner.id!!,
                instance.createdAt
            )
        )

        return newInstance
    }

    fun rejectInstance(id: UUID) {
        val instance = getInstance(id)
        if (instance.status != BookInstance.Status.MODERATION) {
            throw BookInstanceModerationNotNeeded()
        }

        bookInstanceRepository.deleteById(id)
    }

    fun getInstance(id: UUID) =
        bookInstanceRepository.findById(id).orElseThrow { NotFoundException("Book instance not found") }.asModel()

    fun modifyInstance(id: UUID, request: BookInstanceModifyRequest, user: User): BookInstance {
        if (user.role != UserRole.ADMIN) {
            throw AccessDeniedException()
        }

        val instance = getInstance(id)

        var location: LocationEntity? = null
        if (request.locationId != null) {
            location =
                locationRepository.findById(request.locationId).orElseThrow { NotFoundException("Location not found") }
        }

        if (request.photoId != null) {
            attachmentRepo.findById(request.photoId).orElseThrow { NotFoundException("Attachment not found") }
        }

        return bookInstanceRepository.save(
            BookInstanceEntity(
                instance.id,
                instance.bookId,
                request.condition ?: instance.condition,
                instance.owner.asEntity(),
                request.photoId ?: instance.photoId,
                location ?: instance.location.asEntity(),
                request.description ?: instance.description,
                request.status ?: instance.status,
                instance.createdAt
            )
        ).asModel()
    }

    @Transactional
    fun deleteInstance(id: UUID, user: User) {
        val instance = getInstance(id)
        if (user.role != UserRole.ADMIN && user.id != instance.owner.id!!) {
            throw AccessDeniedException("You have to be the owner of this instance")
        }

        transactionRepository.deleteAllByInstanceId(id)
        reservationRepository.deleteAllByInstanceId(id)
        reportRepository.deleteAllByReservationInstanceId(id)
        bookInstanceRepository.deleteById(id)
    }

    fun getIdsInStock(): Set<UUID> =
        bookInstanceRepository.findAllByStatus(BookInstance.Status.PLACED)
            .mapNotNullTo(mutableSetOf()) { bookRepo.findById(it.bookId).getOrNull()?.id }

    fun getAllByIds(ids: Set<UUID>, status: List<BookInstance.Status>): Set<BookInstance> =
        bookInstanceRepository.findAllByIdInAndStatusIn(ids, status).map { it.asModel() }.toSet()

    fun getModerationList() =
        bookInstanceRepository.findAllByStatus(BookInstance.Status.MODERATION).map { it.asModel() }

}
