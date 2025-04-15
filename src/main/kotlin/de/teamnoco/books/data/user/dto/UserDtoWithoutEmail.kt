package de.teamnoco.books.data.user.dto

import de.teamnoco.books.data.user.enum.UserRole
import de.teamnoco.books.data.user.model.User
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(description = "Модель пользователя без email")
data class UserDtoWithoutEmail(
    @Schema(description = "ID пользователя")
    val id: UUID,
    @Schema(description = "Имя пользователя")
    val name: String,
    @Schema(description = "Роль пользователя")
    val role: UserRole
)

fun User.toDtoWithoutEmail() = UserDtoWithoutEmail(id!!, name, role)
