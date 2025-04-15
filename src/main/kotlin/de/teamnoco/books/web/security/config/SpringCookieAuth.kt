package de.teamnoco.books.web.security.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme

@OpenAPIDefinition
@SecurityScheme(
    name = "cookieAuth",
    type = SecuritySchemeType.APIKEY,
    `in` = SecuritySchemeIn.COOKIE,
    paramName = "SESSION",
    description = "Куки 'SESSION', в котором хранится ID сессии. Куки устанавливается после /api/auth/sign-in"
)
class SpringCookieAuth