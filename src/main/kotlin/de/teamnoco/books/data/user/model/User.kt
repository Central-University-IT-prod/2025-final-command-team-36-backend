package de.teamnoco.books.data.user.model

import de.teamnoco.books.data.user.enum.UserRole
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

data class User(
    val id: UUID? = null,
    val email: String,
    val role: UserRole,
    val name: String,
    @get:JvmName("getPassword0")
    val password: String
) : UserDetails {

    override fun getAuthorities() = mutableSetOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun getPassword(): String = password

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

}
