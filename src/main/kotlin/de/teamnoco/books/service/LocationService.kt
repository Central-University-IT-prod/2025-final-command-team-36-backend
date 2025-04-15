package de.teamnoco.books.service

import de.teamnoco.books.data.instance.repository.BookInstanceRepository
import de.teamnoco.books.data.location.dao.LocationEntity
import de.teamnoco.books.data.location.dao.LocationEntity.Companion.asModel
import de.teamnoco.books.data.location.dto.LocationCreateRequest
import de.teamnoco.books.data.location.dto.LocationModifyRequest
import de.teamnoco.books.data.location.model.Location
import de.teamnoco.books.data.location.repository.LocationRepository
import de.teamnoco.books.data.transaction.repository.TransactionRepository
import de.teamnoco.books.data.user.enum.UserRole
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.web.exception.base.AccessDeniedException
import de.teamnoco.books.web.exception.base.NotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

@Service
class LocationService(
    private val locationRepository: LocationRepository,
    private val bookInstanceRepository: BookInstanceRepository,
    private val transactionRepository: TransactionRepository
) {

    @Transactional
    fun create(request: LocationCreateRequest, user: User): Location {
        if (user.role != UserRole.ADMIN) {
            throw AccessDeniedException()
        }

        return locationRepository.save(
            LocationEntity(
                address = request.address,
                extra = request.extra,
                name = request.name,
                limit = request.limit
            )
        ).asModel()
    }

    fun getById(id: UUID): Location =
        locationRepository.findById(id).orElseThrow { NotFoundException("Location not found") }.asModel()

    fun getAll() = locationRepository.findAll().map { it.asModel() }

    fun modify(request: LocationModifyRequest, id: UUID, user: User): Location {
        if (user.role != UserRole.ADMIN) {
            throw AccessDeniedException()
        }

        val location = getById(id)
        return locationRepository.save(
            LocationEntity(
                id,
                location.address,
                request.extra ?: location.extra,
                request.name ?: location.name,
                request.limit ?: location.limit
            )
        ).asModel()
    }

    @Transactional
    fun delete(id: UUID, user: User) {
        if (user.role != UserRole.ADMIN) {
            throw AccessDeniedException()
        }

        getById(id)

        val instanceIds = bookInstanceRepository.findAllByLocationId(id).map { it.id!! }

        transactionRepository.deleteAllByInstanceIdIn(instanceIds)
        bookInstanceRepository.deleteAllById(instanceIds)

        locationRepository.deleteById(id)
    }

}
