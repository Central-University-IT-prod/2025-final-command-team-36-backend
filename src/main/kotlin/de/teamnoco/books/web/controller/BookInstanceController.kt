package de.teamnoco.books.web.controller

import de.teamnoco.books.data.instance.dto.BookInstanceCreateRequest
import de.teamnoco.books.data.instance.dto.BookInstanceModifyRequest
import de.teamnoco.books.data.instance.dto.toDto
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.service.BookInstanceService
import de.teamnoco.books.web.security.annotation.Authorized
import de.teamnoco.books.web.security.annotation.IsAdmin
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "Объявления")
@RestController
@RequestMapping("/api/instances")
class BookInstanceController(private val bookInstanceService: BookInstanceService) {

    @Operation(
        summary = "Получить объявления о книге",
        description = "Получить объявления по ID книги. Доступно без аутентификации.",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID книги")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Успешно получен список книг на модерацию"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Не найдено книги с таким ID",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            )
        ]
    )
    @GetMapping("/book/{id}")
    fun getInstancesByBookId(@PathVariable id: UUID, @AuthenticationPrincipal user: User?) = bookInstanceService.getInstancesByBookId(id, user).map { it.toDto() }

    @Operation(
        summary = "Создать объявление о книге",
        description = "Создать объявление о книге",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Успешно создано объявление"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Не найдено книги с таким ID",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            )
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createInstance(@RequestBody @Valid request: BookInstanceCreateRequest, @AuthenticationPrincipal user: User) =
        bookInstanceService.createInstance(request, user).toDto()

    @Operation(
        summary = "Получить объявление",
        description = "Получить объявление по ID",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID объявления")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Успешно получено объявление"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Не найдено объявление с таким ID",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            )
        ]
    )
    @GetMapping("/{id}")
    fun getInstance(@PathVariable id: UUID) = bookInstanceService.getInstance(id).toDto()

    @Operation(
        summary = "Изменить объявление",
        description = "Изменить объявление. Для пользователя доступно изменение description и condition, для администратора - это и всё остальное",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID объявления")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Успешно изменено объявление"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Не найдено объявление с таким ID",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            )
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @PatchMapping("/{id}")
    fun modifyInstance(
        @PathVariable id: UUID,
        @RequestBody @Valid request: BookInstanceModifyRequest,
        @AuthenticationPrincipal user: User
    ) = bookInstanceService.modifyInstance(id, request, user).toDto()

    @Operation(
        summary = "Удалить объявление",
        description = "Удалить объявление. Все брони и транзакции, связанные с ним, удаляются",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID объявления")],
        responses = [
            ApiResponse(
                responseCode = "204",
                description = "Успешно удалено объявление"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Не найдено объявление с таким ID",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            )
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteInstance(@PathVariable id: UUID, @AuthenticationPrincipal user: User): Unit =
        bookInstanceService.deleteInstance(id, user)

    @Operation(
        summary = "Одобрить объявление",
        description = "Одобрить объявление на модерации. Доступно только для администраторов",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID объявления")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Успешно одобрено объявление"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Не найдено объявление с таким ID",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            )
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @IsAdmin
    @PostMapping("/{id}/approve")
    fun approveInstance(@PathVariable id: UUID) =
        bookInstanceService.approveInstance(id).toDto()

    @Operation(
        summary = "Отклонить объявление",
        description = "Отклонить объявление по итогам модерации. Доступно только для администраторов",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID объявления")],
        responses = [
            ApiResponse(
                responseCode = "204",
                description = "Успешно отклонено объявление"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Не найдено объявление с таким ID",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            )
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @IsAdmin
    @PostMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun rejectInstance(@PathVariable id: UUID) =
        bookInstanceService.rejectInstance(id)

    @Operation(
        summary = "Список объявлений для модерации",
        description = "Получить список объявлений. Доступно только для администраторов",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID объявления")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Успешно получен список объявлений"
            )
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @IsAdmin
    @GetMapping("/moderation-list")
    fun getModerationList() = bookInstanceService.getModerationList().map { it.toDto() }

}