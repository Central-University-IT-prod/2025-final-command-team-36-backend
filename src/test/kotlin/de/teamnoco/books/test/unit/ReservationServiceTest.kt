package de.teamnoco.books.test.unit

import de.teamnoco.books.data.instance.dao.BookInstanceEntity
import de.teamnoco.books.data.instance.dao.BookInstanceEntity.Companion.asEntity
import de.teamnoco.books.data.instance.model.BookInstance
import de.teamnoco.books.data.instance.repository.BookInstanceRepository
import de.teamnoco.books.data.reservation.dao.ReservationEntity
import de.teamnoco.books.data.reservation.dao.ReservationEntity.Companion.asModel
import de.teamnoco.books.data.reservation.exception.ReservationAlreadyExistsException
import de.teamnoco.books.data.reservation.exception.ReservationForbiddenException
import de.teamnoco.books.data.reservation.exception.ReservationLimitException
import de.teamnoco.books.data.reservation.repository.ReservationRepository
import de.teamnoco.books.data.transaction.dao.TransactionEntity
import de.teamnoco.books.data.transaction.repository.TransactionRepository
import de.teamnoco.books.data.user.enum.UserRole
import de.teamnoco.books.service.ReservationService
import de.teamnoco.books.test.util.ModelGenerators.getRandomBookInstance
import de.teamnoco.books.test.util.ModelGenerators.getRandomReservationEntity
import de.teamnoco.books.test.util.ModelGenerators.getRandomUser
import de.teamnoco.books.web.exception.base.NotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.util.*

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
class ReservationServiceTest : StringSpec({

    val reservationRepository = mockk<ReservationRepository>()
    val instanceRepository = mockk<BookInstanceRepository>()
    val transactionRepository = mockk<TransactionRepository>()

    val reservationService = ReservationService(reservationRepository, instanceRepository, transactionRepository)

    every { reservationRepository.findReservationByUserId(any()) } returns listOf()

    "should create reservation" {
        val user = getRandomUser()
        val instanceId = UUID.randomUUID()

        every { instanceRepository.findById(any()) } returns Optional.of(
            getRandomBookInstance().copy(status = BookInstance.Status.PLACED).asEntity()
        )
        every { reservationRepository.findReservationByInstanceId(any()) } returns Optional.empty()

        every { instanceRepository.save(any()) } answers { firstArg<BookInstanceEntity>().copy(id = UUID.randomUUID()) }
        every { reservationRepository.save(any()) } answers { firstArg<ReservationEntity>().copy(id = UUID.randomUUID()) }

        val result = reservationService.createReservation(user, instanceId)

        result.userId shouldBe user.id

        verify { reservationRepository.save(any()) }
        verify { instanceRepository.save(any()) }
    }

    "should throw ReservationAlreadyExistsException when creating a conflicting reservation" {
        val user = getRandomUser()
        val instanceId = UUID.randomUUID()

        every { instanceRepository.findById(any()) } returns Optional.of(getRandomBookInstance().asEntity())
        every { reservationRepository.findReservationByInstanceId(any()) } returns Optional.of(
            getRandomReservationEntity()
        )

        shouldThrow<ReservationAlreadyExistsException> {
            reservationService.createReservation(user, instanceId)
        }
    }

    "should throw ReservationLimitException when user reached a limit of 5 reservations" {
        val user = getRandomUser()

        every { instanceRepository.findById(any()) } returns Optional.of(
            getRandomBookInstance().copy(status = BookInstance.Status.PLACED).asEntity()
        )
        every { reservationRepository.findReservationByInstanceId(any()) } returns Optional.empty()
        every { reservationRepository.findReservationByUserId(any()) } returns (1..5).map { getRandomReservationEntity() }

        shouldThrow<ReservationLimitException> {
            reservationService.createReservation(user, UUID.randomUUID())
        }
    }

    "should throw NotFoundException when instance is not found and trying to create a reservation" {
        val user = getRandomUser()

        every { instanceRepository.findById(any()) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            reservationService.createReservation(user, UUID.randomUUID())
        }
    }

    "should get reservation" {
        val reservation = getRandomReservationEntity()

        every { reservationRepository.findById(any()) } returns Optional.of(reservation)

        val result = reservationService.getReservation(reservation.id!!)
        result shouldBe reservation.asModel()
    }

    "should throw NotFoundException when reservation is not found and trying to get" {
        val id = UUID.randomUUID()

        every { reservationRepository.findById(id) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            reservationService.getReservation(id)
        }
    }

    "should delete reservation" {
        val user = getRandomUser()
        val reservation = getRandomReservationEntity().copy(userId = user.id!!)

        every { reservationRepository.findById(any()) } returns Optional.of(reservation)
        every { instanceRepository.findById(any()) } returns Optional.of(getRandomBookInstance().asEntity())
        every { reservationRepository.deleteById(any()) } just Runs

        reservationService.deleteReservation(reservation.id!!, user)

        verify { reservationRepository.deleteById(reservation.id!!) }
        verify { instanceRepository.save(any()) }
    }

    "should throw NotFoundException when reservation is not found and trying to delete" {
        val user = getRandomUser()
        val id = UUID.randomUUID()

        every { reservationRepository.findById(id) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            reservationService.deleteReservation(id, user)
        }
    }

    "should throw NotFoundException when instance is not found and trying to delete a reservation" {
        val user = getRandomUser()
        val reservation = getRandomReservationEntity().copy(userId = user.id!!)

        every { reservationRepository.findById(any()) } returns Optional.of(reservation)
        every { instanceRepository.findById(any()) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            reservationService.deleteReservation(reservation.id!!, user)
        }
    }

    "should throw ReservationForbiddenException when trying to delete a reservation that does not belong to the user" {
        val user = getRandomUser()
        val reservation = getRandomReservationEntity()

        every { reservationRepository.findById(any()) } returns Optional.of(reservation)

        shouldThrow<ReservationForbiddenException> {
            reservationService.deleteReservation(reservation.id!!, user)
        }
    }

    "should allow ADMIN to delete any reservation" {
        val user = getRandomUser().copy(role = UserRole.ADMIN)
        val reservation = getRandomReservationEntity()

        every { reservationRepository.findById(any()) } returns Optional.of(reservation)
        every { instanceRepository.findById(any()) } returns Optional.of(getRandomBookInstance().asEntity())
        every { reservationRepository.deleteById(any()) } just Runs

        reservationService.deleteReservation(reservation.id!!, user)

        verify { reservationRepository.deleteById(reservation.id!!) }
        verify { instanceRepository.save(any()) }
    }

    "should confirm reservation" {
        val user = getRandomUser()
        val reservation = getRandomReservationEntity().copy(userId = user.id!!)
        val instance = getRandomBookInstance().asEntity().copy(status = BookInstance.Status.RESERVED)

        every { reservationRepository.findById(any()) } returns Optional.of(reservation)
        every { instanceRepository.findById(any()) } returns Optional.of(instance)
        every { transactionRepository.save(any()) } answers { firstArg<TransactionEntity>() }
        every { instanceRepository.save(any()) } answers { firstArg<BookInstanceEntity>() }

        reservationService.confirmReservation(reservation.id!!, user)

        verify { transactionRepository.save(any()) }
        verify { instanceRepository.save(any()) }
        verify { reservationRepository.deleteById(reservation.id!!) }
    }

    "should throw NotFoundException when reservation is not found and trying to confirm" {
        val user = getRandomUser()
        val id = UUID.randomUUID()

        every { reservationRepository.findById(id) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            reservationService.confirmReservation(id, user)
        }
    }

    "should throw NotFoundException when instance is not found and trying to confirm a reservation" {
        val user = getRandomUser()
        val reservation = getRandomReservationEntity().copy(userId = user.id!!)

        every { reservationRepository.findById(any()) } returns Optional.of(reservation)
        every { instanceRepository.findById(any()) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            reservationService.confirmReservation(reservation.id!!, user)
        }
    }

    "should throw ReservationForbiddenException when trying to confirm a reservation that does not belong to the user" {
        val user = getRandomUser()
        val reservation = getRandomReservationEntity()

        every { reservationRepository.findById(any()) } returns Optional.of(reservation)

        shouldThrow<ReservationForbiddenException> {
            reservationService.confirmReservation(reservation.id!!, user)
        }
    }

    "should allow ADMIN to confirm any reservation" {
        val user = getRandomUser().copy(role = UserRole.ADMIN)
        val reservation = getRandomReservationEntity()
        val instance = getRandomBookInstance().asEntity().copy(status = BookInstance.Status.RESERVED)

        every { reservationRepository.findById(any()) } returns Optional.of(reservation)
        every { instanceRepository.findById(any()) } returns Optional.of(instance)
        every { transactionRepository.save(any()) } answers { firstArg<TransactionEntity>() }
        every { instanceRepository.save(any()) } answers { firstArg<BookInstanceEntity>() }

        reservationService.confirmReservation(reservation.id!!, user)

        verify { transactionRepository.save(any()) }
        verify { instanceRepository.save(any()) }
        verify { reservationRepository.deleteById(reservation.id!!) }
    }

})