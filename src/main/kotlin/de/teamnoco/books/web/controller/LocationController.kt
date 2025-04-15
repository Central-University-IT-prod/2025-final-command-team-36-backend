package de.teamnoco.books.web.controller

import de.teamnoco.books.data.location.dto.LocationCreateRequest
import de.teamnoco.books.data.location.dto.LocationModifyRequest
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.service.LocationService
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

@Tag(name = "Локации")
@RestController
@RequestMapping("/api/locations")
class LocationController(private val locationService: LocationService) {

    @Operation(
        summary = "Создать локации",
        description = "Создание локаций. Доступно только администраторам",
        responses = [
            ApiResponse(responseCode = "201", description = "Успешно создано")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @IsAdmin
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createLocation(@RequestBody @Valid request: LocationCreateRequest, @AuthenticationPrincipal user: User) =
        locationService.create(request, user)

    @Operation(
        summary = "Получение локаций",
        description = "Получить все существующие локации",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно получены все локации")
        ]
    )
    @GetMapping
    fun getAllLocations() = locationService.getAll()

    @Operation(
        summary = "Получить локацию",
        description = "Получение локации по ID",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID локации")],
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно получено")
        ]
    )
    @GetMapping("/{id}")
    fun getLocationById(@PathVariable id: UUID) = locationService.getById(id)

    @Operation(
        summary = "Изменить локацию",
        description = "Изменение локации по ID. Доступно только администраторам",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID локации")],
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно изменено")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @IsAdmin
    @PatchMapping("/{id}")
    fun modifyLocation(
        @PathVariable id: UUID,
        @RequestBody @Valid request: LocationModifyRequest,
        @AuthenticationPrincipal user: User
    ) = locationService.modify(request, id, user)

    @Operation(
        summary = "Удалить локацию",
        description = "Удаление локации по ID. Доступно только администраторам",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID локации")],
        responses = [
            ApiResponse(responseCode = "204", description = "Успешно удалено")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @IsAdmin
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteLocation(@PathVariable id: UUID, @AuthenticationPrincipal user: User) = locationService.delete(id, user)
}