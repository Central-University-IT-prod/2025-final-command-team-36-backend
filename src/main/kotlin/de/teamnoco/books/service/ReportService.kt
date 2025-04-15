package de.teamnoco.books.service

import de.teamnoco.books.data.instance.model.BookInstance
import de.teamnoco.books.data.instance.repository.BookInstanceRepository
import de.teamnoco.books.data.report.dao.ReportEntity
import de.teamnoco.books.data.report.dao.ReportEntity.Companion.asEntity
import de.teamnoco.books.data.report.dao.ReportEntity.Companion.asModel
import de.teamnoco.books.data.report.dto.ReportCreateRequest
import de.teamnoco.books.data.report.exception.ReportForbiddenException
import de.teamnoco.books.data.report.model.Report
import de.teamnoco.books.data.report.repository.ReportRepository
import de.teamnoco.books.data.reservation.repository.ReservationRepository
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.web.exception.base.NotFoundException
import org.springframework.stereotype.Service
import java.util.*

@Service
class ReportService(
    private val reportRepository: ReportRepository,
    private val reservationRepository: ReservationRepository,
    private val instanceService: BookInstanceService,
    private val instanceRepository: BookInstanceRepository
) {

    fun getAll() = reportRepository.findAll().map { it.asModel() }

    fun create(request: ReportCreateRequest, user: User): Report {
        val reservation = reservationRepository.findById(request.reservationId)
            .orElseThrow { NotFoundException("Reservation not found") }

        if (reservation.userId != user.id) {
            throw ReportForbiddenException()
        }

        val instance = reservation.instance
        if (instance.status != BookInstance.Status.RESERVED) {
            throw ReportForbiddenException()
        }

        val report = reportRepository.save(
            ReportEntity(
                null,
                reservation,
                request.text
            )
        ).asModel()

        instanceRepository.save(instance.copy(status = BookInstance.Status.REPORTED))
        return report
    }

    fun getReport(id: UUID) =
        reportRepository.findById(id).orElseThrow { NotFoundException("Report not found") }.asModel()

    fun approveReport(id: UUID, user: User) {
        val report = getReport(id)
        reportRepository.deleteById(id)
        instanceService.deleteInstance(report.reservation.instance.id, user)
    }

    fun rejectReport(id: UUID) {
        val report = getReport(id)
        val instance = report.asEntity().reservation.instance
        reportRepository.deleteById(id)
        instanceRepository.save(instance.copy(status = BookInstance.Status.RESERVED))
    }
}