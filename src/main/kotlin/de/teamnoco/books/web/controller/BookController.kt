package de.teamnoco.books.web.controller

import de.teamnoco.books.data.book.dto.BookCreateRequest
import de.teamnoco.books.data.book.dto.BookModifyRequest
import de.teamnoco.books.data.book.model.Book
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.service.BookRecommendationService
import de.teamnoco.books.service.BookService
import de.teamnoco.books.service.BookTrendService
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
import java.time.LocalDateTime
import java.util.*

@Tag(name = "Книги")
@RestController
@RequestMapping("/api/books")
class BookController(
    private val bookService: BookService,
    private val bookTrendService: BookTrendService,
    private val bookRecommendationService: BookRecommendationService
) {

    @IsAdmin
    @Operation(
        summary = "Получить все книги. Доступно только администраторам",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно получены все книги")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping
    fun getAll() = bookService.getAll()

    @Operation(
        summary = "Создать книгу",
        description = "Создание книги. Если пользователь - администратор, то книга сразу же получает status=ACTIVE, иначе отправляется на модерацию.",
        responses = [
            ApiResponse(responseCode = "201", description = "Успешно создана книга"),
            ApiResponse(
                responseCode = "404", description = "Не найдена картинка",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            )
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody @Valid request: BookCreateRequest, @AuthenticationPrincipal user: User) =
        bookService.create(user, request)

    @Operation(
        summary = "Изменить книгу",
        description = "Изменение книги. Доступно только администраторам.",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно изменена книга"),
            ApiResponse(
                responseCode = "404", description = "Не найдена картинка / книга",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            )
        ],
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID книги")]
    )
    @SecurityRequirement(name = "cookieAuth")
    @IsAdmin
    @PatchMapping("/{id}")
    fun modify(@RequestBody @Valid request: BookModifyRequest, @PathVariable id: UUID) = bookService.modify(id, request)

    @Operation(
        summary = "Удалить книгу",
        description = "Удаление книги. Доступно только администраторам.",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID книги")],
        responses = [
            ApiResponse(responseCode = "204", description = "Успешно удалена книга"),
            ApiResponse(
                responseCode = "404", description = "Книга не найдена",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            )
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @IsAdmin
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID) = bookService.delete(id)

    @Operation(
        summary = "Поиск книги",
        parameters = [Parameter(
            name = "query",
            `in` = ParameterIn.QUERY,
            description = "Строка поиска. Здесь может быть использован ISBN, название книги, автор книги или всё вместе."
        )],
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно найдены книги")
        ]
    )
    @GetMapping("/search")
    fun search(@RequestParam query: String) = bookService.search(query)

    @Operation(
        summary = "Получить книгу по ID",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID книги")],
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно найдена книга"),
            ApiResponse(
                responseCode = "404", description = "Книга не найдена",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            )
        ]
    )
    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID) = bookService.getById(id)

    @Operation(
        summary = "Одобрить книгу",
        description = "Одобрение книги при модерации. Доступно только администраторам.",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID книги")],
        responses = [
            ApiResponse(responseCode = "204", description = "Успешно одобрена книга"),
            ApiResponse(
                responseCode = "404", description = "Книга не найдена",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            )
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @IsAdmin
    @PostMapping("/{id}/approve")
    fun approveBook(@PathVariable id: UUID) = bookService.approveBook(id)

    @Operation(
        summary = "Отклонить книгу",
        description = "Отклонить книгу при модерации. Доступно только администрации.",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID книги")],
        responses = [
            ApiResponse(responseCode = "204", description = "Успешно отклонена книга"),
            ApiResponse(
                responseCode = "404", description = "Книга не найдена",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            )
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @IsAdmin
    @PostMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun rejectBook(@PathVariable id: UUID) = bookService.rejectBook(id)

    @Operation(
        summary = "Добавить книгу в избранные",
        description = "Добавить книгу в избранные. Операция идемпотентна.",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID книги")],
        responses = [
            ApiResponse(responseCode = "204", description = "Успешно добавлена книга в избранные"),
            ApiResponse(
                responseCode = "404", description = "Книга не найдена",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            )
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @PostMapping("/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun favorite(@PathVariable id: UUID, @AuthenticationPrincipal user: User) = bookService.favorite(id, user)

    @Operation(
        summary = "Удалить книгу из избранных",
        description = "Удалить книгу из избранных. Операция идемпотентна.",
        parameters = [Parameter(name = "id", `in` = ParameterIn.PATH, description = "ID книги")],
        responses = [
            ApiResponse(responseCode = "204", description = "Успешно удалена книга из избранных"),
            ApiResponse(
                responseCode = "404", description = "Книга не найдена",
                content = [Content(mediaType = "application/json", schema = Schema(ref = "#/components/schemas/Error"))]
            )
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @PostMapping("/{id}/unfavorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unfavorite(@PathVariable id: UUID, @AuthenticationPrincipal user: User) =
        bookService.unfavorite(id, user)

    @Operation(
        summary = "Получить тренды книг",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно получены тренды книг")
        ]
    )
    @GetMapping("/trends")
    fun getTrends(): List<Book> =
        bookTrendService.getBooksWithBorrowTransactions(LocalDateTime.now().minusDays(7)).toList().take(100)
            .map { it.first }

    @Operation(
        summary = "Избранные книги",
        description = "Получить избранные книги авторизованного пользователя",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно получены избранные книги")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @GetMapping("/favorites")
    fun getFavorites(@AuthenticationPrincipal user: User) = bookService.getUserFavorites(user)

    @Operation(
        summary = "Рекомендации",
        description = "Получить рекомендации пользователя",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно получены рекомендации")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @GetMapping("/recommendations")
    fun getRecommendations(
        @AuthenticationPrincipal user: User, @RequestParam limit: Int = 10, @RequestParam offset: Long = 0
    ) = bookRecommendationService.getBooksScoredForUser(user).toList().map { it.first }.drop(offset.toInt()).take(limit)

    @Operation(
        summary = "Список книг на модерацию",
        description = "Получить список книг, подлежащих модерации. Доступно только администраторам.",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешно получен список книг на модерацию")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @IsAdmin
    @GetMapping("/moderation-list")
    fun getModerationList() = bookService.getModerationList()
}
