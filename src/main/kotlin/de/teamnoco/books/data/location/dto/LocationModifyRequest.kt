package de.teamnoco.books.data.location.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Min
import org.hibernate.validator.constraints.Length

@Schema(description = "Запрос на изменение локации")
data class LocationModifyRequest(

    @Schema(description = "Доп. информация о том, как добраться до места")
    @field:NotEmpty
    @field:Length(min = 1)
    val extra: String? = null,

    @Schema(description = "Название локации")
    @field:NotEmpty
    @field:Length(min = 1)
    val name: String? = null,

    @Schema(description = "Лимит локации (макс. кол-во книг)")
    @field:Positive
    @field:Min(1)
    val limit: Int? = null

)
