package de.teamnoco.books.web.security

import de.teamnoco.books.web.response.StatusResponse
import de.teamnoco.books.web.response.sendResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.session.SessionInformationExpiredStrategy
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.servlet.HandlerExceptionResolver

@Component
class SecurityHandler(
    @Qualifier("handlerExceptionResolver") private val resolver: HandlerExceptionResolver,
    private val securityContextRepository: SecurityContextRepository,
) : AuthenticationEntryPoint {
    @Bean
    fun securityFilterChain(
        httpSecurity: HttpSecurity,
        authenticationManager: AuthenticationManager,
        securityPrincipalFilter: SecurityPrincipalFilter
    ): SecurityFilterChain {
        httpSecurity {
            csrf {
                disable()
            }

            cors {
                configurationSource = CorsConfigurationSource {
                    CorsConfiguration().applyPermitDefaultValues().apply {
                        allowedHeaders = listOf("*")
                        allowedMethods = listOf("*")
                        allowedOrigins =
                            listOf("http://localhost:3000", "https://prod-team-36-m2st0u6v.REDACTED")
                        allowCredentials = true
                    }
                }
            }

            authorizeHttpRequests {
                authorize(anyRequest, permitAll)
            }

            exceptionHandling {
                accessDeniedHandler = AccessDeniedHandler { _, response, _ ->
                    response.sendResponse(StatusResponse.Error("Forbidden", 403))
                }

                authenticationEntryPoint = this@SecurityHandler
            }

            securityContext {
                securityContextRepository = this@SecurityHandler.securityContextRepository
            }

            sessionManagement {
                sessionFixation {
                    migrateSession()
                }

                sessionConcurrency {
                    maximumSessions = 15
                    expiredSessionStrategy = SessionInformationExpiredStrategy {
                        it.response.sendResponse(StatusResponse.Error("Credentials expired", 401))
                    }
                }
            }

            this.authenticationManager = authenticationManager

            addFilterBefore<UsernamePasswordAuthenticationFilter>(securityPrincipalFilter)
        }

        return httpSecurity.build()
    }

    override fun commence(
        request: HttpServletRequest, response: HttpServletResponse, authException: AuthenticationException
    ) {
        resolver.resolveException(request, response, null, authException)
    }
}