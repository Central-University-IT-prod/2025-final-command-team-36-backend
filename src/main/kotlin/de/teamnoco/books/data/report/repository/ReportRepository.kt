package de.teamnoco.books.data.report.repository

import de.teamnoco.books.data.report.dao.ReportEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ReportRepository : JpaRepository<ReportEntity, UUID> {
    fun deleteAllByReservationUserId(userId: UUID)

    fun deleteAllByReservationInstanceId(instanceId: UUID)
}
