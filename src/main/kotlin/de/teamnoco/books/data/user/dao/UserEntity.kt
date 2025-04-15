package de.teamnoco.books.data.user.dao

import de.teamnoco.books.data.user.enum.UserRole
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.util.model.EntityConverter
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "users")
data class UserEntity(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(unique = true)
    val email: String,

    @Enumerated(EnumType.STRING)
    val role: UserRole,

    val name: String,

    val password: String

) {

    companion object : EntityConverter<User, UserEntity> {
        override fun UserEntity.asModel() = User(id, email, role, name, password)

        override fun User.asEntity() = UserEntity(id, email, role, name, password)
    }

}
