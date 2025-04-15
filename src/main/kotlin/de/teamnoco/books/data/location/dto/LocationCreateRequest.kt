package de.teamnoco.books.data.location.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive

@Schema(description = "Запрос на создание локации")
data class LocationCreateRequest(

    @Schema(description = "Адрес локации")
    @field:NotEmpty
    val address: String,

    @Schema(description = "Доп. информация о том, как добраться до места")
    @field:NotEmpty
    val extra: String,

    @Schema(description = "Название локации")
    @field:NotEmpty
    val name: String,

    @Schema(description = "Лимит заполнения локации (макс. число книг)")
    @field:Positive
    val limit: Int

)