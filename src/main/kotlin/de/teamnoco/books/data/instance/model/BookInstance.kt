package de.teamnoco.books.data.instance.model

import de.teamnoco.books.data.location.model.Location
import de.teamnoco.books.data.user.model.User
import java.time.LocalDateTime
import java.util.*

data class BookInstance(
    val id: UUID,
    val bookId: UUID,
    val description: String,
    val condition: Condition,
    val owner: User,
    val photoId: UUID,
    val location: Location,
    val status: Status,
    val createdAt: LocalDateTime
) {
    enum class Condition {
        BAD, MEDIUM, GOOD, LIKE_NEW
    }

    enum class Status {
        PLACED, RESERVED, RECEIVED, MODERATION, REPORTED
    }
}