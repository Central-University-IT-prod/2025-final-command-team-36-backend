package de.teamnoco.books.data.reservation.model

import de.teamnoco.books.data.instance.model.BookInstance
import java.time.LocalDateTime
import java.util.*

data class Reservation(
    val id: UUID,
    val instance: BookInstance,
    val userId: UUID,
    val createdAt: LocalDateTime,
    val expireAt: LocalDateTime = createdAt.plusSeconds(60 * 60 * 24 * 7)
)
