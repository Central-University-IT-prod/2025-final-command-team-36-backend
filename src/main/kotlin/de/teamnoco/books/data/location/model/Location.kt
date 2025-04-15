package de.teamnoco.books.data.location.model

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

data class Location(
    @Schema(description = "ID локации")
    val id: UUID,
    @Schema(description = "Адрес локации")
    val address: String,
    @Schema(description = "Доп. информация, как добраться до локации")
    val extra: String,
    @Schema(description = "Название книги")
    val name: String,
    @Schema(description = "Лимит заполнения локации книгами (макс. число книг)")
    val limit: Int
)
