package de.teamnoco.books.data.instance.dto

import de.teamnoco.books.data.instance.model.BookInstance
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import java.util.*

@Schema(description = "Запрос на изменение объявления")
data class BookInstanceModifyRequest(

    @Schema(description = "Описание объявления")
    @field:NotEmpty
    val description: String? = null,

    @Schema(description = "Состояние книги")
    val condition: BookInstance.Condition? = null,

    @Schema(description = "ID на фото экземпляра книги")
    val photoId: UUID? = null,

    @Schema(description = "ID локации, где размещена книга")
    val locationId: UUID? = null,

    @Schema(description = "Статус объявления")
    val status: BookInstance.Status? = null

)