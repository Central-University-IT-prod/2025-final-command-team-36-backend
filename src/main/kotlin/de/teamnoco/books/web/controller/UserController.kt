package de.teamnoco.books.web.controller

import de.teamnoco.books.data.user.dto.UserDto
import de.teamnoco.books.data.user.dto.UserUpdateRequest
import de.teamnoco.books.data.user.dto.toDto
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.service.UserService
import de.teamnoco.books.web.security.annotation.Authorized
import de.teamnoco.books.web.security.annotation.IsAdmin
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "Пользователи")
@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @Operation(
        summary = "Получить всех пользователей",
        description = "Получение всех пользователей. Доступно только для администраторов",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно получены все пользователи")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @IsAdmin
    @GetMapping
    fun getAll() = userService.getAll().map { it.toDto() }

    @Operation(
        summary = "Получить пользователя",
        description = "Получение пользователя по ID. Доступно только администраторам",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID пользователя")],
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно получен пользователь")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @IsAdmin
    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID) = userService.getById(id).toDto()

    @Operation(
        summary = "Изменить пользователя",
        description = "Изменить пользователя по ID. Доступно только администраторам",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID пользователя")],
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно изменен пользователь")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @IsAdmin
    @PatchMapping("/{id}")
    fun modifyUser(@PathVariable id: UUID, @RequestBody @Valid request: UserUpdateRequest) =
        userService.update(id, request).toDto()

    @Operation(
        summary = "Удалить пользователя",
        description = "Удалить пользователя по ID. Доступно только администраторам",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID пользователя")],
        responses = [
            ApiResponse(responseCode = "204", description = "Успешно удален пользователь")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @IsAdmin
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable id: UUID) = userService.delete(id)

    @Operation(
        summary = "Получить своего пользователя",
        description = "Получить своего пользователя. Доступно только с аутентификацией",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно получен свой пользователь")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @GetMapping("/me")
    fun getMe(@AuthenticationPrincipal user: User) = user.toDto()

    @Operation(
        summary = "Изменить своего пользователя",
        description = "Изменить своего пользователя. Доступно только с аутентификацией",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно изменен свой пользователь")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @PatchMapping("/me")
    fun updateMe(@AuthenticationPrincipal user: User, @RequestBody @Valid request: UserUpdateRequest): UserDto =
        userService.update(user.id!!, request).toDto()

    @Operation(
        summary = "Удалить своего пользователя",
        description = "Удалить своего пользователя. Доступно только с аутентификацией",
        responses = [
            ApiResponse(responseCode = "204", description = "Успешно удален свой пользователь")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMe(@AuthenticationPrincipal user: User): Unit = userService.delete(user.id!!)

}
