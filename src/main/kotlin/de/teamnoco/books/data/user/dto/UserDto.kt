package de.teamnoco.books.data.user.dto

import de.teamnoco.books.data.user.enum.UserRole
import de.teamnoco.books.data.user.model.User
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(description = "Пользователь")
data class UserDto(
    @Schema(description = "ID пользователя")
    val id: UUID,
    @Schema(description = "Email пользователя")
    val email: String,
    @Schema(description = "Имя пользователя")
    val name: String,
    @Schema(description = "Роль пользователя")
    val role: UserRole
)

fun User.toDto() = UserDto(id!!, email, name, role)
