package de.teamnoco.books.data.report.model

import de.teamnoco.books.data.reservation.model.Reservation
import java.util.*

data class Report(
    val id: UUID,
    val reservation: Reservation,
    val text: String
)
