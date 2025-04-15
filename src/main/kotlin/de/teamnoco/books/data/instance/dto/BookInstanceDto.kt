package de.teamnoco.books.data.instance.dto

import de.teamnoco.books.data.instance.model.BookInstance
import de.teamnoco.books.data.instance.model.BookInstance.Condition
import de.teamnoco.books.data.instance.model.BookInstance.Status
import de.teamnoco.books.data.location.model.Location
import de.teamnoco.books.data.user.dto.UserDtoWithoutEmail
import de.teamnoco.books.data.user.dto.toDtoWithoutEmail
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.*

@Schema(description = "Объявление")
data class BookInstanceDto(

    @Schema(description = "ID объявления")
    val id: UUID,

    @Schema(description = "ID книги")
    val bookId: UUID,

    @Schema(description = "Описание состояния экземпляра книги")
    val description: String,

    @Schema(description = "Состояние книги (enum)")
    val condition: Condition,

    @Schema(description = "Создатель объявления")
    val owner: UserDtoWithoutEmail,

    @Schema(description = "ID фотографии экземпляра книги")
    val photoId: UUID,

    @Schema(description = "Место, где хранится книга")
    val location: Location,

    @Schema(description = "Статус объявления")
    val status: Status,

    @Schema(description = "Дата создания объявления")
    val createdAt: LocalDateTime

)

fun BookInstance.toDto() = BookInstanceDto(
    id, bookId, description, condition, owner.toDtoWithoutEmail(), photoId, location, status, createdAt
)