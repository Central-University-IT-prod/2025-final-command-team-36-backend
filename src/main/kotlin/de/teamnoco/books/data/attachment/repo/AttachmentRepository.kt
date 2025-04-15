package de.teamnoco.books.data.attachment.repo

import de.teamnoco.books.data.attachment.dao.AttachmentEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
interface AttachmentRepository : JpaRepository<AttachmentEntity, UUID>