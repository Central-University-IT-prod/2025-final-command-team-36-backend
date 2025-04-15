package de.teamnoco.books.data.book.dto

import de.teamnoco.books.data.book.enum.BookSize
import de.teamnoco.books.data.book.model.Book
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length
import java.util.*

@Schema(description = "Запрос на изменение книги")
data class BookModifyRequest(

    @Schema(description = "Название книги")
    @field:Length(min = 1)
    val name: String? = null,

    @Schema(description = "Автор книги")
    @field:Length(min = 1)
    val author: String? = null,

    @Schema(description = "ISBN книги (13 цифр без дефиса)")
    @field:Pattern(regexp = "[0-9]{13}")
    val isbn: String? = null,

    @field:Length(min = 1)
    @Schema(description = "Жанр(ы) книги")
    val genre: String? = null,

    @field:Min(1)
    @Schema(description = "Год издания книги")
    val editionYear: Int? = null,

    @field:Length(min = 1)
    @Schema(description = "Издательская компания")
    val publishingCompany: String? = null,

    @field:Length(min = 1)
    @Schema(description = "Язык книги")
    val language: String? = null,

    @Schema(description = "Тип обложки книги")
    val cover: Book.Cover? = null,

    @field:Min(1)
    @Schema(description = "Количество страниц книги")
    val pages: Int? = null,

    @Schema(description = "Размер книги (enum)")
    val size: BookSize? = null,

    @Schema(description = "ID фото обложки")
    val coverId: UUID? = null

)