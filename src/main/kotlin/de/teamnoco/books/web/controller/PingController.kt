package de.teamnoco.books.web.controller

import de.teamnoco.books.web.response.respondOk
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Пинг")
@RestController
@RequestMapping("/api/ping")
class PingController {

    @Operation(
        summary = "Пингануть приложение",
        responses = [
            ApiResponse(responseCode = "200", description = "Пинг успешен")
        ]
    )
    @GetMapping
    fun ping() = respondOk("АЛЕКСАНДР ШАХОВ Я ВАШ ФАНАТ")

}
