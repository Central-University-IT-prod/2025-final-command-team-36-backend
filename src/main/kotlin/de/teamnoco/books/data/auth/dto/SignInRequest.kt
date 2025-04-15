package de.teamnoco.books.data.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import org.hibernate.validator.constraints.Length

@Schema(description = "Вход в аккаунт")
data class SignInRequest(

    @Schema(description = "Email пользователя")
    @field:Email
    @field:NotEmpty
    val email: String,

    @Schema(description = "Пароль пользователя")
    @field:Length(min = 8)
//    @field:Password
    val password: String

)