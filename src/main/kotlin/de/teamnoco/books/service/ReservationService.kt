package de.teamnoco.books.service

import de.teamnoco.books.data.instance.exception.BookInstanceNotAccessibleException
import de.teamnoco.books.data.instance.model.BookInstance
import de.teamnoco.books.data.instance.repository.BookInstanceRepository
import de.teamnoco.books.data.reservation.dao.ReservationEntity
import de.teamnoco.books.data.reservation.dao.ReservationEntity.Companion.asModel
import de.teamnoco.books.data.reservation.exception.ReservationAlreadyExistsException
import de.teamnoco.books.data.reservation.exception.ReservationForbiddenException
import de.teamnoco.books.data.reservation.exception.ReservationLimitException
import de.teamnoco.books.data.reservation.model.Reservation
import de.teamnoco.books.data.reservation.repository.ReservationRepository
import de.teamnoco.books.data.transaction.dao.TransactionEntity
import de.teamnoco.books.data.transaction.model.Transaction
import de.teamnoco.books.data.transaction.repository.TransactionRepository
import de.teamnoco.books.data.user.enum.UserRole
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.web.exception.base.NotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val instanceRepo: BookInstanceRepository,
    private val transactionRepository: TransactionRepository
) {
    fun createReservation(user: User, instanceId: UUID): Reservation {
        val instance = instanceRepo.findById(instanceId).orElseThrow { NotFoundException("Book instance not found") }
        if (reservationRepository.findReservationByInstanceId(instanceId).isPresent) {
            throw ReservationAlreadyExistsException()
        }

        if (instance.status != BookInstance.Status.PLACED) {
            throw BookInstanceNotAccessibleException()
        }

        val userReservations = getUserReservations(user)
        if (userReservations.size >= 5) {
            throw ReservationLimitException()
        }

        instanceRepo.save(instance.copy(status = BookInstance.Status.RESERVED))

        return reservationRepository.save(
            ReservationEntity(
                instance = instance,
                userId = user.id!!,
                createdAt = LocalDateTime.now()
            )
        ).asModel()
    }

    fun getUserReservations(user: User) = reservationRepository.findReservationByUserId(user.id!!).map { it.asModel() }

    fun getReservation(id: UUID) =
        reservationRepository.findById(id).orElseThrow { NotFoundException("Reservation not found") }.asModel()

    fun deleteReservation(id: UUID, user: User) {
        val reservation = getReservation(id)
        if (reservation.userId != user.id && user.role != UserRole.ADMIN) {
            throw ReservationForbiddenException()
        }

        val instance =
            instanceRepo.findById(reservation.instance.id).orElseThrow { NotFoundException("Book instance not found") }
        instanceRepo.save(instance.copy(status = BookInstance.Status.PLACED))
        reservationRepository.deleteById(id)
    }

    fun confirmReservation(id: UUID, user: User) {
        val reservation = getReservation(id)
        if (reservation.userId != user.id && user.role != UserRole.ADMIN) {
            throw ReservationForbiddenException()
        }

        val instance =
            instanceRepo.findById(reservation.instance.id).orElseThrow { NotFoundException("Book instance not found") }

        if (instance.status != BookInstance.Status.RESERVED) {
            throw ReservationForbiddenException()
        }

        transactionRepository.save(
            TransactionEntity(
                null,
                Transaction.Type.BORROW,
                reservation.instance.id,
                user.id!!,
                LocalDateTime.now()
            )
        )

        instanceRepo.save(instance.copy(status = BookInstance.Status.RECEIVED))
        reservationRepository.deleteById(id)
    }
}