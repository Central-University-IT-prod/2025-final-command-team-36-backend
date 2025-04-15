package de.teamnoco.books.test.util

import de.teamnoco.books.data.user.model.User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken

fun setupMockUser(user: User) {
    SecurityContextHolder.getContext().authentication = PreAuthenticatedAuthenticationToken(
        user, null, user.authorities
    )
}
