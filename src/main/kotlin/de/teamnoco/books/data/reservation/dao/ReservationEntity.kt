package de.teamnoco.books.data.reservation.dao

import de.teamnoco.books.data.instance.dao.BookInstanceEntity
import de.teamnoco.books.data.instance.dao.BookInstanceEntity.Companion.asEntity
import de.teamnoco.books.data.instance.dao.BookInstanceEntity.Companion.asModel
import de.teamnoco.books.data.reservation.model.Reservation
import de.teamnoco.books.data.user.dao.UserEntity
import de.teamnoco.books.util.model.EntityConverter
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "reservations")
data class ReservationEntity(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    val instance: BookInstanceEntity,

    @Column(name = "user_id")
    val userId: UUID,

    val createdAt: LocalDateTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    val user: UserEntity? = null

) {

    companion object : EntityConverter<Reservation, ReservationEntity> {
        override fun ReservationEntity.asModel(): Reservation =
            Reservation(
                id!!,
                instance.asModel(),
                userId,
                createdAt
            )

        override fun Reservation.asEntity(): ReservationEntity =
            ReservationEntity(
                id,
                instance.asEntity(),
                userId,
                createdAt
            )

    }
}
