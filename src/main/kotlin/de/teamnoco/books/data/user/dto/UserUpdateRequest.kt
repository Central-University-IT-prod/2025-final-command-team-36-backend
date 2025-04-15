package de.teamnoco.books.data.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

@Schema(description = "Запрос на обновление пользователя")
data class UserUpdateRequest(

    @Schema(description = "Имя пользователя")
    @field:NotEmpty
    val name: String?

)