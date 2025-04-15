package de.teamnoco.books.data.location.dao

import de.teamnoco.books.data.location.model.Location
import de.teamnoco.books.util.model.EntityConverter
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "locations")
data class LocationEntity(
    @Id
    @GeneratedValue
    val id: UUID? = null,
    val address: String,
    val extra: String,
    val name: String,
    @Column(name = "limit_") // i hate hibernate
    val limit: Int
) {
    companion object : EntityConverter<Location, LocationEntity> {
        override fun LocationEntity.asModel(): Location = Location(id!!, address, extra, name, limit)

        override fun Location.asEntity(): LocationEntity = LocationEntity(id, address, extra, name, limit)
    }
}