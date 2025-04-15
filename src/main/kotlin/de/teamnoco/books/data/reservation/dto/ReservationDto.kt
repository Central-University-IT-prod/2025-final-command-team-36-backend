package de.teamnoco.books.data.reservation.dto

import de.teamnoco.books.data.instance.dto.BookInstanceDto
import de.teamnoco.books.data.instance.dto.toDto
import de.teamnoco.books.data.reservation.model.Reservation
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.*

@Schema(description = "Бронь книги")
data class ReservationDto(

    @Schema(description = "ID брони")
    val id: UUID,

    @Schema(description = "Объявление, с которым связана бронь")
    val instance: BookInstanceDto,

    @Schema(description = "ID пользователя, который забронировал книгу")
    val userId: UUID,

    @Schema(description = "Дата создания брони")
    val createdAt: LocalDateTime,

    @Schema(description = "Дата истечения брони")
    val expireAt: LocalDateTime = createdAt.plusSeconds(60 * 60 * 24 * 7)

)

fun Reservation.toDto() = ReservationDto(id, instance.toDto(), userId, createdAt, expireAt)
