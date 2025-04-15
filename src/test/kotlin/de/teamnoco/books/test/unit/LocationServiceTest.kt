package de.teamnoco.books.test.unit

import de.teamnoco.books.data.instance.dao.BookInstanceEntity
import de.teamnoco.books.data.instance.repository.BookInstanceRepository
import de.teamnoco.books.data.location.dao.LocationEntity
import de.teamnoco.books.data.location.dao.LocationEntity.Companion.asModel
import de.teamnoco.books.data.location.repository.LocationRepository
import de.teamnoco.books.data.transaction.repository.TransactionRepository
import de.teamnoco.books.data.user.enum.UserRole
import de.teamnoco.books.service.LocationService
import de.teamnoco.books.test.util.ModelGenerators.getRandomLocationCreateRequest
import de.teamnoco.books.test.util.ModelGenerators.getRandomLocationModifyRequest
import de.teamnoco.books.test.util.ModelGenerators.getRandomUser
import de.teamnoco.books.web.exception.base.AccessDeniedException
import de.teamnoco.books.web.exception.base.NotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.util.*

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
class LocationServiceTest : StringSpec({

    val locationRepository = mockk<LocationRepository>()
    val bookInstanceRepository = mockk<BookInstanceRepository>()
    val transactionRepository = mockk<TransactionRepository>()

    val locationService = LocationService(locationRepository, bookInstanceRepository, transactionRepository)

    "should create location" {
        val user = getRandomUser().copy(role = UserRole.ADMIN)
        val request = getRandomLocationCreateRequest()

        every { locationRepository.save(any()) } answers { firstArg<LocationEntity>().copy(id = UUID.randomUUID()) }

        val result = locationService.create(request, user)

        result.name shouldBe request.name
        result.address shouldBe request.address
        result.extra shouldBe request.extra
        result.limit shouldBe request.limit

        verify { locationRepository.save(any()) }
    }

    "should throw AccessDeniedException if user is not admin and tries to create a location" {
        val user = getRandomUser().copy(role = UserRole.USER)
        val request = getRandomLocationCreateRequest()

        shouldThrow<AccessDeniedException> {
            locationService.create(request, user)
        }
    }

    "should get by id" {
        val location = LocationEntity(
            id = UUID.randomUUID(),
            name = "name",
            address = "address",
            extra = "extra",
            limit = 10
        )

        every { locationRepository.findById(location.id!!) } returns Optional.of(location)

        val result = locationService.getById(location.id!!)
        result shouldBe location.asModel()
    }

    "should throw NotFoundException if location is not found" {
        val id = UUID.randomUUID()

        every { locationRepository.findById(id) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            locationService.getById(id)
        }
    }

    "should get all" {
        val locations = (1..50).map {
            LocationEntity(
                id = UUID.randomUUID(),
                name = "name",
                address = "address",
                extra = "extra",
                limit = 10
            )
        }

        every { locationRepository.findAll() } returns locations

        val result = locationService.getAll()
        result shouldBe locations.map { it.asModel() }
    }

    "should modify location" {
        val user = getRandomUser().copy(role = UserRole.ADMIN)
        val location = LocationEntity(
            id = UUID.randomUUID(),
            name = "name",
            address = "address",
            extra = "extra",
            limit = 10
        )

        val request = getRandomLocationModifyRequest()

        every { locationRepository.findById(location.id!!) } returns Optional.of(location)
        every { locationRepository.save(any()) } answers { firstArg<LocationEntity>() }

        val result = locationService.modify(request, location.id!!, user)

        result.name shouldBe request.name
        result.address shouldBe location.address
        result.extra shouldBe request.extra
        result.limit shouldBe request.limit

        verify { locationRepository.save(any()) }
    }

    "should throw AccessDeniedException if user is not admin and tries to update a location" {
        val user = getRandomUser().copy(role = UserRole.USER)
        val location = LocationEntity(
            id = UUID.randomUUID(),
            name = "name",
            address = "address",
            extra = "extra",
            limit = 10
        )

        val request = getRandomLocationModifyRequest()

        shouldThrow<AccessDeniedException> {
            locationService.modify(request, location.id!!, user)
        }
    }

    "should delete location" {
        val user = getRandomUser().copy(role = UserRole.ADMIN)
        val location = LocationEntity(
            id = UUID.randomUUID(),
            name = "name",
            address = "address",
            extra = "extra",
            limit = 10
        )

        every { locationRepository.findById(location.id!!) } returns Optional.of(location)
        every { locationRepository.deleteById(location.id!!) } just Runs
        every { bookInstanceRepository.findAllByLocationId(location.id!!) } returns emptyList()
        every { transactionRepository.deleteAllByInstanceIdIn(emptyList()) } just Runs
        every { bookInstanceRepository.deleteAllById(emptyList()) } just Runs

        locationService.delete(location.id!!, user)

        verify { locationRepository.deleteById(location.id!!) }
    }

    "should throw AccessDeniedException if user is not admin and tries to delete a location" {
        val user = getRandomUser().copy(role = UserRole.USER)
        val location = LocationEntity(
            id = UUID.randomUUID(),
            name = "name",
            address = "address",
            extra = "extra",
            limit = 10
        )

        shouldThrow<AccessDeniedException> {
            locationService.delete(location.id!!, user)
        }
    }

})