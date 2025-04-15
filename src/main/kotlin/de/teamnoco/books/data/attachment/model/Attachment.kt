package de.teamnoco.books.data.attachment.model

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(description = "Изображение")
data class Attachment(
    @Schema(description = "ID изображения")
    val id: UUID,
    @Schema(description = "Расширение файла изображения")
    val extension: String?,
    @Schema(description = "Content-Type изображения")
    val contentType: String
)