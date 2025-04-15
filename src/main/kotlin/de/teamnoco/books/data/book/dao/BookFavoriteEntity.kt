package de.teamnoco.books.data.book.dao

import de.teamnoco.books.data.user.dao.UserEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "book_favorites")
data class BookFavoriteEntity(

    @EmbeddedId
    val id: Id,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    val user: UserEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", referencedColumnName = "id", insertable = false, updatable = false)
    val bookEntity: BookEntity? = null

) {

    @Embeddable
    data class Id(

        @Column(name = "user_id")
        val userId: UUID,

        @Column(name = "book_id")
        val bookId: UUID

    )

}