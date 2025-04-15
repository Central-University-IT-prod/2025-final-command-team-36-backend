package de.teamnoco.books.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.servers.Server
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
class ApplicationConfig {

    @Bean
    fun openApi(): OpenAPI {
        val errorSchema = Schema<Any>().apply {
            `$ref` = "#/components/schemas/Error"
        }

        val errorContent = Content().addMediaType(
            "application/json",
            MediaType().schema(errorSchema)
        )

        return OpenAPI().servers(
            listOf(
                Server().apply { url = "https://prod-team-36-m2st0u6v.REDACTED" })
        ).info(
            Info().title("Books API Documentation").version("v1").description(
                """
                            made with <3 by the No-Code Team
                        """.trimIndent()
            ).contact(
                Contact().name("GitHub").url("https://teamnoco.de/")
            )
        ).components(Components().apply {
            addResponses("404", ApiResponse().description("Объект(ы) не найден(ы)").content(errorContent))
            addResponses("413", ApiResponse().description("Слишком большой запрос").content(errorContent))
            addResponses("415", ApiResponse().description("Неподдерживаемый тип данных").content(errorContent))
        })
    }

}