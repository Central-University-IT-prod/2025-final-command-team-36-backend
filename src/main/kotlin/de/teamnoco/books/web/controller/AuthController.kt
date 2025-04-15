package de.teamnoco.books.web.controller

import de.teamnoco.books.data.auth.dto.ChangePasswordRequest
import de.teamnoco.books.data.auth.dto.RegisterRequest
import de.teamnoco.books.data.auth.dto.SignInRequest
import de.teamnoco.books.data.user.dto.UserDto
import de.teamnoco.books.data.user.dto.toDto
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.service.AuthenticationService
import de.teamnoco.books.web.security.annotation.Authorized
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Аутентификация")
@RestController
@RequestMapping("/api/auth")
class AuthController(private val authenticationService: AuthenticationService) {

    @Operation(
        summary = "Зарегистрировать нового пользователя",
        responses = [
            ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
            ApiResponse(
                responseCode = "409",
                description = "Пользователь с таким email уже существует",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            ),
        ]
    )
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody @Valid request: RegisterRequest): UserDto = authenticationService.register(
        email = request.email, password = request.password, name = request.name
    ).toDto()

    @Operation(
        summary = "Войти в аккаунт пользователя",
        responses = [
            ApiResponse(responseCode = "200", description = "Пользователь успешно вошел в аккаунт"),
            ApiResponse(
                responseCode = "401", description = "Неверный email или пароль",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            )
        ]
    )
    @PostMapping("/sign-in")
    fun signIn(
        httpRequest: HttpServletRequest, httpResponse: HttpServletResponse, @RequestBody @Valid request: SignInRequest
    ): UserDto =
        authenticationService.signIn(httpRequest, httpResponse, email = request.email, password = request.password)
            .toDto()

    @Operation(
        summary = "Выйти из аккаунта",
        responses = [
            ApiResponse(responseCode = "204", description = "Пользователь успешно вышел из аккаунта")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @PostMapping("/log-out")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(request: HttpServletRequest, response: HttpServletResponse, @AuthenticationPrincipal user: User): Unit =
        authenticationService.logOut(user, request, response)

    @Operation(
        summary = "Сменить пароль",
        responses = [
            ApiResponse(responseCode = "204", description = "Пароль успешно изменен"),
            ApiResponse(
                responseCode = "401", description = "Неверный старый пароль",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            )
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun changePassword(
        @AuthenticationPrincipal user: User,
        @RequestBody @Valid request: ChangePasswordRequest,
        httpRequest: HttpServletRequest
    ): Unit = authenticationService.changePassword(user, request.oldPassword, request.newPassword, httpRequest)

}
