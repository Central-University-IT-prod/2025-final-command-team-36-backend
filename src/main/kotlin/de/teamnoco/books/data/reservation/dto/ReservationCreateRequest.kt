package de.teamnoco.books.data.reservation.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(description = "Запрос на бронирование книги")
data class ReservationCreateRequest(

    @Schema(description = "ID объявления")
    val instanceId: UUID

)