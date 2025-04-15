package de.teamnoco.books.data.instance.dao

import de.teamnoco.books.data.attachment.dao.AttachmentEntity
import de.teamnoco.books.data.book.dao.BookEntity
import de.teamnoco.books.data.instance.model.BookInstance
import de.teamnoco.books.data.location.dao.LocationEntity
import de.teamnoco.books.data.location.dao.LocationEntity.Companion.asEntity
import de.teamnoco.books.data.location.dao.LocationEntity.Companion.asModel
import de.teamnoco.books.data.user.dao.UserEntity
import de.teamnoco.books.data.user.dao.UserEntity.Companion.asEntity
import de.teamnoco.books.data.user.dao.UserEntity.Companion.asModel
import de.teamnoco.books.util.model.EntityConverter
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "book_instances")
data class BookInstanceEntity(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(name = "book_id")
    val bookId: UUID,

    @Enumerated(EnumType.STRING)
    val condition: BookInstance.Condition,

    @ManyToOne(fetch = FetchType.LAZY)
    val owner: UserEntity,

    @Column(name = "photo_id")
    val photoId: UUID,

    @ManyToOne(fetch = FetchType.LAZY)
    val location: LocationEntity,

    val description: String,

    @Enumerated(EnumType.STRING)
    val status: BookInstance.Status,

    val createdAt: LocalDateTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id", referencedColumnName = "id", insertable = false, updatable = false)
    val photoAttachment: AttachmentEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", referencedColumnName = "id", insertable = false, updatable = false)
    val book: BookEntity? = null

) {

    companion object : EntityConverter<BookInstance, BookInstanceEntity> {
        override fun BookInstanceEntity.asModel(): BookInstance = BookInstance(
            id!!,
            bookId,
            description,
            condition,
            owner.asModel(),
            photoId,
            location.asModel(),
            status,
            createdAt
        )

        override fun BookInstance.asEntity(): BookInstanceEntity = BookInstanceEntity(
            id,
            bookId,
            condition,
            owner.asEntity(),
            photoId,
            location.asEntity(),
            description,
            status,
            createdAt
        )

    }

}
