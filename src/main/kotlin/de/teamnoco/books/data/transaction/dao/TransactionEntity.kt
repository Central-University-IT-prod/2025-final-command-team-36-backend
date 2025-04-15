package de.teamnoco.books.data.transaction.dao

import de.teamnoco.books.data.instance.dao.BookInstanceEntity
import de.teamnoco.books.data.transaction.model.Transaction
import de.teamnoco.books.data.user.dao.UserEntity
import de.teamnoco.books.util.model.EntityConverter
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "transactions")
data class TransactionEntity(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    val type: Transaction.Type,

    @Column(name = "instance_id")
    val instanceId: UUID,

    @Column(name = "user_id")
    val userId: UUID,

    val createdAt: LocalDateTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    val user: UserEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id", referencedColumnName = "id", insertable = false, updatable = false)
    val instance: BookInstanceEntity? = null

) {
    companion object : EntityConverter<Transaction, TransactionEntity> {
        override fun TransactionEntity.asModel(): Transaction = Transaction(
            id!!,
            type,
            instanceId,
            userId,
            createdAt
        )

        override fun Transaction.asEntity(): TransactionEntity = TransactionEntity(
            id,
            type,
            instanceId,
            userId,
            createdAt
        )

    }
}
