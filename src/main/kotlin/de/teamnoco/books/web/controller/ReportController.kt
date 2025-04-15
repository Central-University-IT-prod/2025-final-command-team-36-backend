package de.teamnoco.books.web.controller

import de.teamnoco.books.data.report.dto.ReportCreateRequest
import de.teamnoco.books.data.report.dto.toDto
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.service.ReportService
import de.teamnoco.books.web.security.annotation.Authorized
import de.teamnoco.books.web.security.annotation.IsAdmin
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Репорты")
@RestController
@RequestMapping("/api/reports")
class ReportController(private val reportService: ReportService) {

    @Operation(summary = "Получить все репорты")
    @IsAdmin
    @GetMapping
    fun getAll() = reportService.getAll().map { it.toDto() }

    @Operation(summary = "Создать репорт")
    @Authorized
    @PostMapping
    fun createReport(@RequestBody request: ReportCreateRequest, @AuthenticationPrincipal user: User) =
        reportService.create(request, user).toDto()

    @Operation(summary = "Получить репорт")
    @IsAdmin
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun getReport(@PathVariable id: UUID) = reportService.getReport(id).toDto()

    @Operation(summary = "Подтвердить репорт")
    @IsAdmin
    @PostMapping("/{id}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun approveReport(@PathVariable id: UUID, @AuthenticationPrincipal user: User) = reportService.approveReport(id, user)

    @Operation(summary = "Отклонить репорт")
    @IsAdmin
    @PostMapping("/{id}/reject")
    fun rejectReport(@PathVariable id: UUID) = reportService.rejectReport(id)

}