package de.teamnoco.books.data.reservation.repository

import de.teamnoco.books.data.reservation.dao.ReservationEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ReservationRepository : JpaRepository<ReservationEntity, UUID> {

    fun findReservationByInstanceId(instanceId: UUID): Optional<ReservationEntity>

    fun findReservationByUserId(userId: UUID): List<ReservationEntity>

    fun deleteAllByInstanceId(instanceId: UUID)
}