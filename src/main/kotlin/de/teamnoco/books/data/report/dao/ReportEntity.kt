package de.teamnoco.books.data.report.dao

import de.teamnoco.books.data.report.model.Report
import de.teamnoco.books.data.reservation.dao.ReservationEntity
import de.teamnoco.books.data.reservation.dao.ReservationEntity.Companion.asEntity
import de.teamnoco.books.data.reservation.dao.ReservationEntity.Companion.asModel
import de.teamnoco.books.util.model.EntityConverter
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "reports")
data class ReportEntity(
    @Id
    @GeneratedValue
    val id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    val reservation: ReservationEntity,
    val text: String
) {
    companion object : EntityConverter<Report, ReportEntity> {
        override fun ReportEntity.asModel(): Report = Report(
            id!!, reservation.asModel(), text
        )

        override fun Report.asEntity(): ReportEntity = ReportEntity(
            id, reservation.asEntity(), text
        )

    }
}
