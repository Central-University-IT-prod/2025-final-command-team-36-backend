package de.teamnoco.books.web.security

import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.service.UserService
import de.teamnoco.books.web.response.StatusResponse
import de.teamnoco.books.web.response.sendResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class SecurityPrincipalFilter(
    private val userService: UserService,
    private val securityBeanPostProcessor: SecurityBeanPostProcessor
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        runCatching {
            val authentication = SecurityContextHolder.getContext().authentication
            if (securityBeanPostProcessor.requiresAuth(request.method, request.requestURI)) {
                if (authentication == null || !authentication.isAuthenticated) {
                    response.sendResponse(StatusResponse.Error("Unauthorized", 401))
                    return
                }
            }

            if (authentication is UsernamePasswordAuthenticationToken && authentication.isAuthenticated) {
                val user = userService.getById((authentication.principal as User).id!!)

                SecurityContextHolder.getContext().authentication =
                    UsernamePasswordAuthenticationToken(user, null, user.authorities)
            }
        }.onFailure { e ->
            e.printStackTrace()
            response.sendResponse(StatusResponse.Error("Invalid credentials", 401))
        }.onSuccess {
            filterChain.doFilter(request, response)
        }
    }
}