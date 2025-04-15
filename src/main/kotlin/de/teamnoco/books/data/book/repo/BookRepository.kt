package de.teamnoco.books.data.book.repo

import de.teamnoco.books.data.book.dao.BookEntity
import de.teamnoco.books.data.book.model.Book
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface BookRepository : JpaRepository<BookEntity, UUID> {

    @Query(
        """
        SELECT *,
          GREATEST(
            ts_rank(
              to_tsvector('russian', coalesce(name, '') || ' ' || coalesce(author, '')),
              websearch_to_tsquery('russian', :searchQuery)
            ),
            similarity(lower(coalesce(name, '')), lower(:searchQuery)),
            similarity(lower(coalesce(author, '')), lower(:searchQuery))
          ) AS rank
        FROM books
        WHERE status = 'ACTIVE'
          AND (
             isbn = :searchQuery
             OR (
                 websearch_to_tsquery('russian', :searchQuery) IS NOT NULL
                 AND to_tsvector('russian', coalesce(name, '') || ' ' || coalesce(author, ''))
                     @@ websearch_to_tsquery('russian', :searchQuery)
             )
             OR similarity(lower(coalesce(name, '')), lower(:searchQuery)) > 0.7
             OR similarity(lower(coalesce(author, '')), lower(:searchQuery)) > 0.7
          )
        ORDER BY rank DESC
        """, nativeQuery = true
    )
    fun search(searchQuery: String, pageable: Pageable): List<BookEntity>

    fun findAllByIdInAndStatus(ids: Set<UUID>, status: Book.Status = Book.Status.ACTIVE): List<BookEntity>

    @Query("SELECT b FROM BookEntity b WHERE b.status = :status")
    fun getModerationList(status: Book.Status = Book.Status.MODERATION): List<BookEntity>

}