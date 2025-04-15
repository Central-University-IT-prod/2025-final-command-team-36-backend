package de.teamnoco.books.data.attachment.dao

import de.teamnoco.books.data.attachment.model.Attachment
import de.teamnoco.books.util.model.EntityConverter
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "attachments")
data class AttachmentEntity(

    @Id
    @GeneratedValue
    val id: UUID? = null, // id - ключ в s3

    val extension: String?,

    val contentType: String,

) {
    companion object : EntityConverter<Attachment, AttachmentEntity> {
        override fun AttachmentEntity.asModel(): Attachment = Attachment(
            id!!, extension!!, contentType
        )

        override fun Attachment.asEntity(): AttachmentEntity = AttachmentEntity(
            id, extension, contentType
        )

    }
}