package de.teamnoco.books.data.book.dao

import de.teamnoco.books.data.attachment.dao.AttachmentEntity
import de.teamnoco.books.data.book.enum.BookSize
import de.teamnoco.books.data.book.model.Book
import de.teamnoco.books.util.model.EntityConverter
import jakarta.persistence.*
import java.util.*

@Table(name = "books")
@Entity
data class BookEntity(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    val name: String,

    val author: String,

    val isbn: String?, // 13 digits

    val genre: String,

    val editionYear: Int,

    val publishingCompany: String,

    val language: String,

    @Enumerated(EnumType.STRING)
    val cover: Book.Cover,

    val pages: Int,

    @Enumerated(EnumType.STRING)
    val size: BookSize,

    @Column(name = "cover_id")
    val coverId: UUID,

    @Enumerated(EnumType.STRING)
    val status: Book.Status,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_id", referencedColumnName = "id", insertable = false, updatable = false)
    val coverAttachment: AttachmentEntity? = null,

    ) {

    companion object : EntityConverter<Book, BookEntity> {

        override fun BookEntity.asModel(): Book = Book(
            id = id!!,
            name = name,
            author = author,
            isbn = isbn,
            genre = genre,
            editionYear = editionYear,
            publishingCompany = publishingCompany,
            language = language,
            cover = cover,
            pages = pages,
            size = size,
            coverId = coverId,
            status = status
        )

        override fun Book.asEntity(): BookEntity = BookEntity(
            id = id,
            name = name,
            author = author,
            isbn = isbn,
            genre = genre,
            editionYear = editionYear,
            publishingCompany = publishingCompany,
            language = language,
            cover = cover,
            pages = pages,
            size = size,
            coverId = coverId,
            status = status
        )

    }

}