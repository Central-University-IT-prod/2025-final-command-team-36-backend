package de.teamnoco.books.web.controller

import de.teamnoco.books.data.attachment.dao.AttachmentEntity.Companion.asModel
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.service.AttachmentService
import de.teamnoco.books.web.security.annotation.Authorized
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
@Tag(name = "Изображения")
@RestController
@RequestMapping("/api/attachments")
class AttachmentController(private val attachmentService: AttachmentService) {

    @Operation(
        summary = "Загрузить изображение",
        requestBody = RequestBody(
            description = "Изображение в виде multipart file, поле file",
            content = [Content(mediaType = "multipart/form-data")]
        ),
        responses = [
            ApiResponse(responseCode = "201", description = "Изображение успешно загружено"),
            ApiResponse(responseCode = "413", ref = "#/components/responses/413"),
            ApiResponse(responseCode = "415", ref = "#/components/responses/415")
        ]
    )
    @SecurityRequirement(name = "cookieAuth")
    @Authorized
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = [MULTIPART_FORM_DATA_VALUE])
    fun uploadAttachment(
        @RequestParam file: MultipartFile,
        @AuthenticationPrincipal user: User
    ) = attachmentService.upload(file = file, user = user).asModel()

    @Operation(
        summary = "Получить модель изображения",
        responses = [
            ApiResponse(responseCode = "200", description = "Изображение успешно получено"),
            ApiResponse(
                responseCode = "404",
                ref = "#/components/responses/404",
                description = "Изображение не найдено"
            )
        ]
    )
    @GetMapping("/{id}")
    fun getAttachment(@PathVariable id: UUID) = attachmentService.getById(id).asModel()

    @Operation(
        summary = "Скачать содержание изображения",
        responses = [
            ApiResponse(responseCode = "200", description = "Изображение успешно получено"),
            ApiResponse(
                responseCode = "404",
                ref = "#/components/responses/404",
                description = "Изображение не найдено"
            )
        ]
    )
    @ApiResponses(value = [ApiResponse(description = "Изображение", content = [Content(mediaType = "images/*")])])
    @GetMapping("/{id}/content")
    fun getContent(@PathVariable id: UUID, @RequestParam download: Boolean = false): ResponseEntity<ByteArrayResource> =
        attachmentService.downloadAttachment(id, download)

}