package de.teamnoco.books.data.transaction.model

import java.time.LocalDateTime
import java.util.*

data class Transaction(
    val id: UUID,
    val type: Type,
    val instanceId: UUID,
    val userId: UUID,
    val createdAt: LocalDateTime
) {

    enum class Type {
        BORROW,
        LEND
    }

}