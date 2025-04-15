package de.teamnoco.books.service

import de.teamnoco.books.data.attachment.dao.AttachmentEntity
import de.teamnoco.books.data.attachment.repo.AttachmentRepository
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.web.exception.base.InvalidBodyException
import de.teamnoco.books.web.exception.base.NotFoundException
import de.teamnoco.books.web.exception.base.PayloadTooLargeException
import de.teamnoco.books.web.exception.base.UnsupportedMediaTypeException
import io.awspring.cloud.s3.S3Template
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
@Service
class AttachmentService(
    private val s3Template: S3Template,
    @Value("\${spring.cloud.aws.s3.bucket}") private val bucketName: String,
    private val attachmentRepository: AttachmentRepository,
) {

    fun upload(file: MultipartFile, user: User): AttachmentEntity {
        if (file.size == 0L) {
            throw InvalidBodyException("File must not be empty")
        }

        if (file.size >= 15 * 1024 * 1024) {
            throw PayloadTooLargeException("File size must be less than 15 MB")
        }

        val extension = file.originalFilename?.substringAfterLast(".")
        val type = file.contentType ?: "application/octet-stream"

        val mediaType: MediaType
        try {
            mediaType = MediaType.parseMediaType(
                type
            )
        } catch (_: Exception) {
            throw UnsupportedMediaTypeException("Unsupported media type")
        }

        if (!listOf(MediaType.IMAGE_PNG, MediaType.IMAGE_JPEG).contains(mediaType)) {
            throw UnsupportedMediaTypeException("File must be of type PNG, JPG (JPEG)")
        }

        val attachmentEntity = attachmentRepository.save(
            AttachmentEntity(
                extension = extension, contentType = type
            )
        )

        s3Template.upload(
            bucketName, attachmentEntity.id.toString(), file.inputStream
        )

        return attachmentEntity
    }

    fun getById(id: UUID): AttachmentEntity {
        return attachmentRepository.findById(id).orElseThrow { NotFoundException("Attachment with id $id not found") }
    }

    fun downloadAttachment(id: UUID, download: Boolean): ResponseEntity<ByteArrayResource> {
        val attachment = getById(id)

        val result = s3Template.download(bucketName, id.toString())
        val resource = ByteArrayResource(result.contentAsByteArray)

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(attachment.contentType)).header(
            HttpHeaders.CONTENT_DISPOSITION,
            "${if (download) "attachment" else "inline"}; filename=${attachment.id}.${attachment.extension}"
        ).body(resource)
    }

    init {

        try {
            if (!s3Template.bucketExists(bucketName)) {
                s3Template.createBucket(bucketName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}