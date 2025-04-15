package de.teamnoco.books.data.user.repository

import de.teamnoco.books.data.user.dao.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<UserEntity, UUID> {

    fun findByEmail(username: String): Optional<UserEntity>

    fun existsByEmail(username: String): Boolean

}
