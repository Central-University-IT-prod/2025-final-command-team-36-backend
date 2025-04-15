package de.teamnoco.books.data.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import org.hibernate.validator.constraints.Length

@Schema(description = "Запрос на смену пароля")
data class ChangePasswordRequest(

    @Schema(description = "Старый пароль")
    @field:NotEmpty
    val oldPassword: String,

    @Schema(description = "Новый пароль")
    @field:Length(min = 8)
//    @field:Password
    val newPassword: String

)