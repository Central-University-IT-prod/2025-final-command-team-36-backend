package de.teamnoco.books.test.unit

import de.teamnoco.books.data.report.repository.ReportRepository
import de.teamnoco.books.data.user.dao.UserEntity.Companion.asEntity
import de.teamnoco.books.data.user.dto.UserUpdateRequest
import de.teamnoco.books.data.user.repository.UserRepository
import de.teamnoco.books.service.UserService
import de.teamnoco.books.test.util.ModelGenerators.getRandomUser
import de.teamnoco.books.web.exception.base.NotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.session.SessionRegistry
import java.util.*

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
class UserServiceTest : StringSpec({

    val userRepository = mockk<UserRepository>()
    val sessionRegistry = mockk<SessionRegistry>()
    val jdbcTemplate = mockk<JdbcTemplate>()
    val reportRepository = mockk<ReportRepository>()

    val userService = UserService(userRepository, reportRepository, sessionRegistry, jdbcTemplate)

    "should get by id" {
        val user = getRandomUser()

        every { userRepository.findById(user.id!!) } returns Optional.of(user.asEntity())

        val result = userService.getById(user.id!!)
        result shouldBe user
    }

    "should throw NotFoundException if user not found by id" {
        val user = getRandomUser()

        every { userRepository.findById(user.id!!) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            userService.getById(user.id!!)
        }
    }

    "should get by email" {
        val user = getRandomUser()

        every { userRepository.findByEmail(user.email) } returns Optional.of(user.asEntity())

        val result = userService.getByEmail(user.email)
        result shouldBe user
    }

    "should throw NotFoundException if user not found by email" {
        val user = getRandomUser()

        every { userRepository.findByEmail(user.email) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            userService.getByEmail(user.email)
        }
    }

    "should check if user exists by email" {
        val user = getRandomUser()

        every { userRepository.existsByEmail(user.email) } returns true

        val result = userService.existsByEmail(user.email)
        result shouldBe true
    }

    "should update user" {
        val user = getRandomUser()
        val request = UserUpdateRequest(name = "new_user_name")
        val expectedUser = user.copy(name = request.name!!)

        every { userRepository.findById(user.id!!) } returns Optional.of(user.asEntity())
        every { userRepository.save(any()) } answers { firstArg() }

        val result = userService.update(user.id!!, request)
        result shouldBe expectedUser

        verify { userRepository.save(expectedUser.asEntity()) }
    }

    "should not change anything if request is all nulls" {
        val user = getRandomUser()
        val request = UserUpdateRequest(name = null)

        every { userRepository.findById(user.id!!) } returns Optional.of(user.asEntity())

        val result = userService.update(user.id!!, request)
        result shouldBe user
    }

    "should delete user" {
        val user = getRandomUser()

        every { userRepository.findById(user.id!!) } returns Optional.of(user.asEntity())
        every { userRepository.deleteById(user.asEntity().id!!) } just Runs
        every { sessionRegistry.getAllSessions(any(), any()) } returns emptyList()
        every { reportRepository.deleteAllByReservationUserId(user.id!!) } just Runs

        userService.delete(user.id!!)

        verify { userRepository.deleteById(user.asEntity().id!!) }
        verify { sessionRegistry.getAllSessions(any(), any()) }
    }

})