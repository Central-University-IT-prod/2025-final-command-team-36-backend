package de.teamnoco.books.test.unit

import de.teamnoco.books.data.attachment.dao.AttachmentEntity.Companion.asEntity
import de.teamnoco.books.data.attachment.repo.AttachmentRepository
import de.teamnoco.books.data.book.dao.BookEntity.Companion.asEntity
import de.teamnoco.books.data.book.exception.BookNotFoundException
import de.teamnoco.books.data.book.repo.BookRepository
import de.teamnoco.books.data.instance.dao.BookInstanceEntity
import de.teamnoco.books.data.instance.dao.BookInstanceEntity.Companion.asEntity
import de.teamnoco.books.data.instance.dto.BookInstanceModifyRequest
import de.teamnoco.books.data.instance.exception.BookInstanceModerationNotNeeded
import de.teamnoco.books.data.instance.model.BookInstance
import de.teamnoco.books.data.instance.repository.BookInstanceRepository
import de.teamnoco.books.data.location.dao.LocationEntity.Companion.asEntity
import de.teamnoco.books.data.location.repository.LocationRepository
import de.teamnoco.books.data.report.repository.ReportRepository
import de.teamnoco.books.data.reservation.repository.ReservationRepository
import de.teamnoco.books.data.transaction.dao.TransactionEntity
import de.teamnoco.books.data.transaction.repository.TransactionRepository
import de.teamnoco.books.data.user.dao.UserEntity.Companion.asEntity
import de.teamnoco.books.data.user.enum.UserRole
import de.teamnoco.books.data.user.repository.UserRepository
import de.teamnoco.books.service.BookInstanceService
import de.teamnoco.books.test.util.ModelGenerators.getRandomAttachment
import de.teamnoco.books.test.util.ModelGenerators.getRandomBook
import de.teamnoco.books.test.util.ModelGenerators.getRandomBookInstance
import de.teamnoco.books.test.util.ModelGenerators.getRandomBookInstanceCreateRequest
import de.teamnoco.books.test.util.ModelGenerators.getRandomBookInstanceModifyRequest
import de.teamnoco.books.test.util.ModelGenerators.getRandomLocation
import de.teamnoco.books.test.util.ModelGenerators.getRandomUser
import de.teamnoco.books.web.exception.base.AccessDeniedException
import de.teamnoco.books.web.exception.base.NotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.util.*

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
class BookInstanceServiceTest : StringSpec({

    val bookRepository = mockk<BookRepository>()
    val bookInstanceRepository = mockk<BookInstanceRepository>()
    val attachmentRepository = mockk<AttachmentRepository>()
    val locationRepository = mockk<LocationRepository>()
    val transactionRepository = mockk<TransactionRepository>()
    val userRepository = mockk<UserRepository>()
    val reservationRepository = mockk<ReservationRepository>()
    val reportRepository = mockk<ReportRepository>()

    val bookInstanceService = BookInstanceService(
        bookRepository,
        bookInstanceRepository,
        attachmentRepository,
        locationRepository,
        transactionRepository,
        reservationRepository,
        reportRepository
    )

    "should get instances by book id" {
        val book = getRandomBook()
        val instances = (1..10).map { getRandomBookInstance().copy(bookId = book.id) }

        every { bookRepository.findById(book.id) } returns Optional.of(book.asEntity())
        every {
            bookInstanceRepository.findInstancesByBookIdAndStatusIn(
                book.id, any()
            )
        } returns instances.map { it.asEntity() }

        val result = bookInstanceService.getInstancesByBookId(book.id, null)

        result.size shouldBe 10
        result shouldContainAll instances
    }

    "should throw BookNotFoundException if book not found" {
        val bookId = UUID.randomUUID()

        every { bookRepository.findById(bookId) } returns Optional.empty()

        shouldThrow<BookNotFoundException> {
            bookInstanceService.getInstancesByBookId(bookId, null)
        }
    }

    "should create instance" {
        val user = getRandomUser()
        val request = getRandomBookInstanceCreateRequest()
        val location = getRandomLocation()
        val attachment = getRandomAttachment()

        every { bookRepository.findById(request.bookId) } returns Optional.of(getRandomBook().asEntity())
        every { attachmentRepository.findById(request.photoId) } returns Optional.of(attachment.asEntity())
        every { locationRepository.findById(request.locationId) } returns Optional.of(location.asEntity())
        every { userRepository.findById(user.id!!) } returns Optional.of(user.asEntity())

        every { bookInstanceRepository.save(any()) } answers { firstArg<BookInstanceEntity>().copy(id = UUID.randomUUID()) }

        val result = bookInstanceService.createInstance(request, user)

        result.bookId shouldBe request.bookId
        result.condition shouldBe request.condition
        result.owner shouldBe user
        result.photoId shouldBe request.photoId
        result.location shouldBe location
        result.description shouldBe request.description
        result.status shouldBe BookInstance.Status.MODERATION
    }

    "should skip moderation for ADMIN when creating" {
        val user = getRandomUser().copy(role = UserRole.ADMIN)
        val request = getRandomBookInstanceCreateRequest()
        val location = getRandomLocation()
        val attachment = getRandomAttachment()

        every { bookRepository.findById(request.bookId) } returns Optional.of(getRandomBook().asEntity())
        every { attachmentRepository.findById(request.photoId) } returns Optional.of(attachment.asEntity())
        every { locationRepository.findById(request.locationId) } returns Optional.of(location.asEntity())
        every { userRepository.findById(user.id!!) } returns Optional.of(user.asEntity())

        every { bookInstanceRepository.save(any()) } answers { firstArg<BookInstanceEntity>().copy(id = UUID.randomUUID()) }
        every { transactionRepository.save(any()) } answers { firstArg<TransactionEntity>().copy(id = UUID.randomUUID()) }

        val result = bookInstanceService.createInstance(request, user)

        result.status shouldBe BookInstance.Status.PLACED

        verify { transactionRepository.save(match { it.instanceId == result.id }) }
    }

    "should throw BookNotFoundException if book not found when creating" {
        val user = getRandomUser()
        val request = getRandomBookInstanceCreateRequest()

        every { bookRepository.findById(request.bookId) } returns Optional.empty()

        shouldThrow<BookNotFoundException> {
            bookInstanceService.createInstance(request, user)
        }
    }

    "should throw NotFoundException if photo not found when creating" {
        val user = getRandomUser()
        val request = getRandomBookInstanceCreateRequest()

        every { bookRepository.findById(request.bookId) } returns Optional.of(getRandomBook().asEntity())
        every { attachmentRepository.findById(request.photoId) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            bookInstanceService.createInstance(request, user)
        }
    }

    "should throw NotFoundException if location not found when creating" {
        val user = getRandomUser()
        val request = getRandomBookInstanceCreateRequest()
        val attachment = getRandomAttachment()

        every { bookRepository.findById(request.bookId) } returns Optional.of(getRandomBook().asEntity())
        every { attachmentRepository.findById(request.photoId) } returns Optional.of(attachment.asEntity())
        every { locationRepository.findById(request.locationId) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            bookInstanceService.createInstance(request, user)
        }
    }

    "should approve instance" {
        val instance = getRandomBookInstance().copy(status = BookInstance.Status.MODERATION)

        every { bookInstanceRepository.findById(instance.id) } returns Optional.of(instance.asEntity())
        every { bookInstanceRepository.save(any()) } answers { firstArg<BookInstanceEntity>().copy(id = UUID.randomUUID()) }
        every { transactionRepository.save(any()) } answers { firstArg<TransactionEntity>().copy(id = UUID.randomUUID()) }

        val result = bookInstanceService.approveInstance(instance.id)

        result.status shouldBe BookInstance.Status.PLACED

        println(result)
        verify { transactionRepository.save(match { it.instanceId == instance.id }) }
    }

    "should throw BookInstanceModerationNotNeeded if instance not in moderation status" {
        val instance = getRandomBookInstance().copy(status = BookInstance.Status.PLACED)

        every { bookInstanceRepository.findById(instance.id) } returns Optional.of(instance.asEntity())

        shouldThrow<BookInstanceModerationNotNeeded> {
            bookInstanceService.approveInstance(instance.id)
        }
    }

    "should throw NotFoundException if instance not found when approving" {
        val instanceId = UUID.randomUUID()

        every { bookInstanceRepository.findById(instanceId) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            bookInstanceService.approveInstance(instanceId)
        }
    }

    "should reject instance" {
        val instance = getRandomBookInstance().copy(status = BookInstance.Status.MODERATION)

        every { bookInstanceRepository.findById(instance.id) } returns Optional.of(instance.asEntity())
        every { bookInstanceRepository.deleteById(instance.id) } just Runs

        bookInstanceService.rejectInstance(instance.id)

        verify { bookInstanceRepository.deleteById(instance.id) }
    }

    "should throw BookInstanceModerationNotNeeded if instance not in moderation status when rejecting" {
        val instance = getRandomBookInstance().copy(status = BookInstance.Status.PLACED)

        every { bookInstanceRepository.findById(instance.id) } returns Optional.of(instance.asEntity())

        shouldThrow<BookInstanceModerationNotNeeded> {
            bookInstanceService.rejectInstance(instance.id)
        }
    }

    "should throw NotFoundException if instance not found when rejecting" {
        val instanceId = UUID.randomUUID()

        every { bookInstanceRepository.findById(instanceId) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            bookInstanceService.rejectInstance(instanceId)
        }
    }

    "should modify instance" {
        val user = getRandomUser().copy(role = UserRole.ADMIN)
        val instance = getRandomBookInstance().copy(owner = user)
        val request = BookInstanceModifyRequest(
            description = "asdhasjhdajkshasdjksdh", condition = BookInstance.Condition.entries.random()
        )

        every { bookInstanceRepository.findById(instance.id) } returns Optional.of(instance.asEntity())

        every { bookInstanceRepository.save(any()) } answers { firstArg<BookInstanceEntity>().copy(id = UUID.randomUUID()) }

        val result = bookInstanceService.modifyInstance(instance.id, request, user)

        result.description shouldBe request.description
        result.condition shouldBe request.condition
    }

    "should throw AccessDeniedException when trying to update unmodifiable fields as a USER" {
        val user = getRandomUser().copy(role = UserRole.USER)
        val instance = getRandomBookInstance().copy(owner = user)
        val request = BookInstanceModifyRequest(locationId = UUID.randomUUID())

        every { bookInstanceRepository.findById(instance.id) } returns Optional.of(instance.asEntity())

        shouldThrow<AccessDeniedException> {
            bookInstanceService.modifyInstance(instance.id, request, user)
        }
    }

    "should not throw AccessDeniedException when trying to update unmodifiable fields as an ADMIN" {
        val user = getRandomUser().copy(role = UserRole.ADMIN)
        val instance = getRandomBookInstance().copy(owner = user)
        val location = getRandomLocation()
        val request = BookInstanceModifyRequest(locationId = location.id)

        every { userRepository.findById(instance.owner.id!!) } returns Optional.of(instance.owner.asEntity())
        every { locationRepository.findById(location.id) } returns Optional.of(location.asEntity())
        every { bookInstanceRepository.findById(instance.id) } returns Optional.of(instance.asEntity())

        val result = bookInstanceService.modifyInstance(instance.id, request, user)

        result.location.id shouldBe request.locationId
    }

    "should not throw AccessDeniedException when trying to update others' instances as an ADMIN" {
        val user = getRandomUser().copy(role = UserRole.ADMIN)
        val instance = getRandomBookInstance().copy(owner = getRandomUser())

        every { userRepository.findById(instance.owner.id!!) } returns Optional.of(instance.owner.asEntity())
        every { userRepository.findById(user.id!!) } returns Optional.of(user.asEntity())
        every { bookInstanceRepository.findById(instance.id) } returns Optional.of(instance.asEntity())

        val result = bookInstanceService.modifyInstance(
            instance.id, BookInstanceModifyRequest(description = "asdasda"), user
        )

        result.owner shouldBe instance.owner
    }

    "should throw NotFoundException if location not found when modifying" {
        val user = getRandomUser().copy(role = UserRole.ADMIN)
        val instance = getRandomBookInstance().copy(owner = user)
        val request = BookInstanceModifyRequest(locationId = UUID.randomUUID())

        every { userRepository.findById(instance.owner.id!!) } returns Optional.of(instance.owner.asEntity())
        every { bookInstanceRepository.findById(instance.id) } returns Optional.of(instance.asEntity())
        every { locationRepository.findById(request.locationId!!) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            bookInstanceService.modifyInstance(instance.id, request, user)
        }
    }

    "should throw NotFoundException if attachment not found when modifying" {
        val user = getRandomUser().copy(role = UserRole.ADMIN)
        val instance = getRandomBookInstance().copy(owner = user)
        val request = BookInstanceModifyRequest(photoId = UUID.randomUUID())

        every { userRepository.findById(instance.owner.id!!) } returns Optional.of(instance.owner.asEntity())
        every { bookInstanceRepository.findById(instance.id) } returns Optional.of(instance.asEntity())
        every { attachmentRepository.findById(request.photoId!!) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            bookInstanceService.modifyInstance(instance.id, request, user)
        }
    }

    "should throw AccessDeniedException when trying to update others' instances as a USER" {
        val user = getRandomUser().copy(role = UserRole.USER)
        val instance = getRandomBookInstance().copy(owner = getRandomUser())

        every { bookInstanceRepository.findById(instance.id) } returns Optional.of(instance.asEntity())

        shouldThrow<AccessDeniedException> {
            bookInstanceService.modifyInstance(instance.id, getRandomBookInstanceModifyRequest(), user)
        }
    }

    "should delete instance" {
        val user = getRandomUser()
        val instance = getRandomBookInstance().copy(owner = user)

        every { bookInstanceRepository.findById(instance.id) } returns Optional.of(instance.asEntity())
        every { bookInstanceRepository.deleteById(instance.id) } just Runs
        every { transactionRepository.deleteAllByInstanceId(instance.id) } just Runs
        every { reservationRepository.deleteAllByInstanceId(instance.id) } just Runs
        every { reportRepository.deleteAllByReservationInstanceId(instance.id) } just Runs

        bookInstanceService.deleteInstance(instance.id, user)

        verify { bookInstanceRepository.deleteById(instance.id) }
        verify { transactionRepository.deleteAllByInstanceId(instance.id) }
    }

    "should throw AccessDeniedException when trying to delete others' instances as a USER" {
        val user = getRandomUser().copy(role = UserRole.USER)
        val instance = getRandomBookInstance().copy(owner = getRandomUser())

        every { bookInstanceRepository.findById(instance.id) } returns Optional.of(instance.asEntity())

        shouldThrow<AccessDeniedException> {
            bookInstanceService.deleteInstance(instance.id, user)
        }
    }

    "should not throw AccessDeniedException when trying to delete others' instances as an ADMIN" {
        val user = getRandomUser().copy(role = UserRole.ADMIN)
        val instance = getRandomBookInstance().copy(owner = getRandomUser())

        every { bookInstanceRepository.findById(instance.id) } returns Optional.of(instance.asEntity())
        every { userRepository.findById(user.id!!) } returns Optional.of(user.asEntity())
        every { transactionRepository.deleteAllByInstanceId(instance.id) } just Runs
        every { bookInstanceRepository.deleteById(instance.id) } just Runs
        every { reservationRepository.deleteAllByInstanceId(instance.id) } just Runs
        every { reportRepository.deleteAllByReservationInstanceId(instance.id) } just Runs

        bookInstanceService.deleteInstance(instance.id, user)

        verify { bookInstanceRepository.deleteById(instance.id) }
        verify { transactionRepository.deleteAllByInstanceId(instance.id) }
    }

    "should throw NotFoundException if instance not found when deleting" {
        val instanceId = UUID.randomUUID()

        every { bookInstanceRepository.findById(instanceId) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            bookInstanceService.deleteInstance(instanceId, getRandomUser())
        }
    }

    "should get moderation list" {
        val instances = (1..10).map { getRandomBookInstance().copy(status = BookInstance.Status.MODERATION) }

        every { bookInstanceRepository.findAllByStatus(BookInstance.Status.MODERATION) } returns instances.map { it.asEntity() }
            .toSet()

        val result = bookInstanceService.getModerationList()

        result.size shouldBe 10
        result shouldContainAll instances
    }

    "should get all by ids" {
        val instances = (1..10).map { getRandomBookInstance() }

        every { bookInstanceRepository.findAllByIdInAndStatusIn(any(), any()) } returns instances.map { it.asEntity() }
            .toSet()

        val result = bookInstanceService.getAllByIds(instances.map { it.id }.toSet(), instances.map { it.status })

        result.size shouldBe 10
        result shouldContainAll instances
    }

    "should get ids in stock" {
        val instances = (1..10).map { getRandomBookInstance().copy(status = BookInstance.Status.PLACED) }

        every { bookInstanceRepository.findAllByStatus(BookInstance.Status.PLACED) } returns instances.map { it.asEntity() }
            .toSet()
        every { bookRepository.findById(any()) } answers {
            Optional.of(
                getRandomBook().copy(id = firstArg()).asEntity()
            )
        }

        val result = bookInstanceService.getIdsInStock()

        result.size shouldBe 10
        result shouldContainAll instances.map { it.bookId }
    }

})