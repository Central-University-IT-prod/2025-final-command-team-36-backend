package de.teamnoco.books.data.book.repo

import de.teamnoco.books.data.book.dao.BookFavoriteEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
interface BookFavoriteRepository : JpaRepository<BookFavoriteEntity, BookFavoriteEntity.Id> {

    fun findAllByUserId(id: UUID): List<BookFavoriteEntity>

}