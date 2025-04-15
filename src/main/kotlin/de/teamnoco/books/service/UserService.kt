package de.teamnoco.books.service

import de.teamnoco.books.data.report.repository.ReportRepository
import de.teamnoco.books.data.user.dao.UserEntity.Companion.asEntity
import de.teamnoco.books.data.user.dao.UserEntity.Companion.asModel
import de.teamnoco.books.data.user.dto.UserUpdateRequest
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.data.user.repository.UserRepository
import de.teamnoco.books.web.exception.base.NotFoundException
import jakarta.annotation.PostConstruct
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.session.SessionRegistry
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val reportRepository: ReportRepository,
    private val sessionRegistry: SessionRegistry,
    private val jdbcTemplate: JdbcTemplate
) {

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @PostConstruct
    fun init() {
        jdbcTemplate.execute(javaClass.classLoader.getResource("setup_users.sql").readText())
    }

    fun getAll() = userRepository.findAll().map { it.asModel() }

    fun getById(id: UUID): User {
        val user = userRepository.findById(id).orElseThrow { NotFoundException("User not found") }

        return user.asModel()
    }

    fun getByEmail(username: String): User {
        val user = userRepository.findByEmail(username).orElseThrow { NotFoundException("User not found") }

        return user.asModel()
    }

    fun existsByEmail(username: String) = userRepository.existsByEmail(username)

    fun update(userId: UUID, request: UserUpdateRequest): User {
        val user = getById(userId)
        val newUser = user.copy(name = request.name ?: user.name)
        userRepository.save(newUser.asEntity())

        return newUser
    }

    @Transactional
    fun delete(userId: UUID) {
        getById(userId)
        reportRepository.deleteAllByReservationUserId(userId)
        userRepository.deleteById(userId)
        sessionRegistry.getAllSessions(userId.toString(), false).forEach {
            it.expireNow()
        }
    }

}