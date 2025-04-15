package de.teamnoco.books.data.report.dto

import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class ReportCreateRequest(
    val reservationId: UUID,

    @field:NotBlank
    val text: String
)
