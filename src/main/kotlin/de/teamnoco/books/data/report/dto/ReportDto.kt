package de.teamnoco.books.data.report.dto

import de.teamnoco.books.data.report.model.Report
import de.teamnoco.books.data.reservation.dto.ReservationDto
import de.teamnoco.books.data.reservation.dto.toDto
import java.util.UUID

data class ReportDto(
    val id: UUID,
    val reservation: ReservationDto,
    val text: String
)

fun Report.toDto() = ReportDto(id, reservation.toDto(), text)
