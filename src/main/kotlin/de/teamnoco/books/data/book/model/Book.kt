package de.teamnoco.books.data.book.model

import de.teamnoco.books.data.book.enum.BookSize
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(description = "Книга")
data class Book(
    @Schema(description = "ID книги")
    val id: UUID,
    @Schema(description = "Название книги")
    val name: String,
    @Schema(description = "Автор книги")
    val author: String,
    @Schema(description = "ISBN книги (13 цифр без дефиса)")
    val isbn: String?, // 13 digits
    @Schema(description = "Жанр(ы) книги")
    val genre: String,
    @Schema(description = "Год издания")
    val editionYear: Int,
    @Schema(description = "Издательская компания")
    val publishingCompany: String,
    @Schema(description = "Язык книги")
    val language: String,
    @Schema(description = "Тип обложки книги")
    val cover: Cover,
    @Schema(description = "Кол-во страниц книги")
    val pages: Int,
    @Schema(description = "Размер книги (enum)")
    val size: BookSize,
    @Schema(description = "ID фотографии обложки")
    val coverId: UUID,
    @Schema(description = "Статус книги")
    val status: Status
) {

    enum class Cover {
        HARD,
        SOFT
    }

    enum class Status {
        MODERATION,
        ACTIVE
    }

}
