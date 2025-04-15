package de.teamnoco.books.data.location.repository

import de.teamnoco.books.data.location.dao.LocationEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface LocationRepository : JpaRepository<LocationEntity, UUID>
