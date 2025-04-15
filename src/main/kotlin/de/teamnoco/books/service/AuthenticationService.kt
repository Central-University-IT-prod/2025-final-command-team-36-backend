package de.teamnoco.books.service

import de.teamnoco.books.data.auth.exception.BadPasswordException
import de.teamnoco.books.data.user.dao.UserEntity.Companion.asEntity
import de.teamnoco.books.data.user.dao.UserEntity.Companion.asModel
import de.teamnoco.books.data.user.enum.UserRole
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.data.user.repository.UserRepository
import de.teamnoco.books.web.exception.base.ConflictException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val authenticationManager: AuthenticationManager,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val securityContextRepository: SecurityContextRepository,
    private val sessionRegistry: SessionRegistry
) {

    fun register(email: String, password: String, name: String): User {
        if (userService.existsByEmail(email)) {
            throw ConflictException("User with this email already exists")
        }

        val user = User(
            email = email,
            password = passwordEncoder.encode(password),
            name = name,
            role = UserRole.USER
        )

        return userRepository.save(user.asEntity()).asModel()
    }

    fun signIn(
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse,
        email: String,
        password: String
    ): User {
        try {
            val auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, password)
            )

            val contextHolder = SecurityContextHolder.getContextHolderStrategy()
            val context = contextHolder.createEmptyContext()

            context.authentication = auth
            contextHolder.context = context

            securityContextRepository.saveContext(context, httpRequest, httpResponse)

            return userService.getByEmail(email)
        } catch (ex: AuthenticationException) {
            throw BadCredentialsException("", ex)
        }
    }

    fun logOut(user: User, request: HttpServletRequest, response: HttpServletResponse) {
        sessionRegistry.getSessionInformation(request.requestedSessionId).expireNow()
        SecurityContextHolder.getContext().authentication?.let {
            SecurityContextLogoutHandler().logout(request, response, it)
        }
    }

    fun changePassword(user: User, oldPassword: String, newPassword: String, request: HttpServletRequest) {
        if (!passwordEncoder.matches(oldPassword, user.password)) throw BadPasswordException()

        val userEntity = user.asEntity()
            .copy(password = passwordEncoder.encode(newPassword))

        userRepository.save(userEntity)

        sessionRegistry.getAllSessions(user.id.toString(), false).forEach {
            if (it.sessionId != request.requestedSessionId) {
                it.expireNow()
            }
        }
    }

}