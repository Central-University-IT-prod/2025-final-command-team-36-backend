package de.teamnoco.books.data.instance.repository

import de.teamnoco.books.data.instance.dao.BookInstanceEntity
import de.teamnoco.books.data.instance.model.BookInstance
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface BookInstanceRepository : JpaRepository<BookInstanceEntity, UUID> {

    fun findInstancesByBookIdAndStatusIn(bookId: UUID, status: List<BookInstance.Status>): List<BookInstanceEntity>

    fun findInstancesByBookId(bookId: UUID): List<BookInstanceEntity>

    fun findAllByStatus(status: BookInstance.Status): Set<BookInstanceEntity>

    fun findAllByIdInAndStatusIn(ids: Set<UUID>, status: List<BookInstance.Status>): Set<BookInstanceEntity>

    fun deleteAllByBookId(bookId: UUID)

    fun deleteAllByLocationId(locationId: UUID)

    fun findAllByLocationId(locationId: UUID): List<BookInstanceEntity>

}
