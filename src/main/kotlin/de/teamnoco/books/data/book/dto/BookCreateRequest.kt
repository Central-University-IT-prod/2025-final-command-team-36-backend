package de.teamnoco.books.data.book.dto

import de.teamnoco.books.data.book.enum.BookSize
import de.teamnoco.books.data.book.model.Book
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import java.util.*

@Schema(description = "Запрос на создание книги")
data class BookCreateRequest(

    @Schema(description = "Название книги")
    @field:NotEmpty
    val name: String,

    @Schema(description = "Автор книги")
    @field:NotEmpty
    val author: String,

    @Schema(description = "ISBN книги (13 цифр без дефисов)")
    @field:Pattern(regexp = "[0-9]{13}")
    val isbn: String?,

    @Schema(description = "Жанр(ы) книги")
    @field:NotEmpty
    val genre: String,

    @Schema(description = "Год издания")
    @field:Positive
    val editionYear: Int,

    @Schema(description = "Издательская компания")
    @field:NotEmpty
    val publishingCompany: String,

    @Schema(description = "Язык")
    @field:NotEmpty
    val language: String,

    @Schema(description = "Тип обложки книги")
    val cover: Book.Cover,

    @Schema(description = "Кол-во страниц")
    @field:Positive
    val pages: Int,

    @Schema(description = "Размер книги (enum)")
    val size: BookSize,

    @Schema(description = "ID фотографии обложки книги")
    val coverId: UUID

)