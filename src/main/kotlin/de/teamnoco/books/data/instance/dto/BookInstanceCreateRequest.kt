package de.teamnoco.books.data.instance.dto

import de.teamnoco.books.data.instance.model.BookInstance
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(description = "Запрос на создание объявления")
data class BookInstanceCreateRequest(

    @Schema(description = "ID книги")
    val bookId: UUID,

    @Schema(description = "Описание состояния книги")
    val description: String = "",

    @Schema(description = "Состояние книги (enum)")
    val condition: BookInstance.Condition,

    @Schema(description = "ID фото книги")
    val photoId: UUID,

    @Schema(description = "ID локации, где лежит книга")
    val locationId: UUID

)