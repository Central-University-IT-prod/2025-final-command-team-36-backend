package de.teamnoco.books.web.controller

import de.teamnoco.books.data.reservation.dto.ReservationCreateRequest
import de.teamnoco.books.data.reservation.dto.toDto
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.service.ReservationService
import de.teamnoco.books.web.security.annotation.Authorized
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

@Tag(name = "Брони")
@RestController
@RequestMapping("/api/reservations")
class ReservationController(private val reservationService: ReservationService) {

    @Operation(
        summary = "Создать бронь",
        description = "Создать бронь на книгу",
        responses = [
            ApiResponse(responseCode = "201", description = "Успешно создано")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createReservation(@RequestBody @Valid request: ReservationCreateRequest, @AuthenticationPrincipal user: User) =
        reservationService.createReservation(user, request.instanceId).toDto()

    @Operation(
        summary = "Получить брони пользователя",
        description = "Получение всех броней пользователя",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно получены все брони пользователя")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @GetMapping
    fun getUserReservations(@AuthenticationPrincipal user: User) =
        reservationService.getUserReservations(user).map { it.toDto() }

    @Operation(
        summary = "Получить бронь",
        description = "Получение брони по ID",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID брони")],
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно получено")
        ]
    )
    @GetMapping("/{id}")
    fun getReservation(@PathVariable id: UUID) = reservationService.getReservation(id).toDto()

    @Operation(
        summary = "Отменить бронь",
        description = "Отмена брони",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID брони")],
        responses = [
            ApiResponse(responseCode = "204", description = "Успешно удалено")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteReservation(@PathVariable id: UUID, @AuthenticationPrincipal user: User) =
        reservationService.deleteReservation(id, user)

    @Operation(
        summary = "Завершить бронь",
        description = "Подтвердить взятие книги и завершить бронь",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID брони")],
        responses = [
            ApiResponse(responseCode = "204", description = "Успешно завершено")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @PostMapping("/{id}/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun confirmReservation(@PathVariable id: UUID, @AuthenticationPrincipal user: User) =
        reservationService.confirmReservation(id, user)
}