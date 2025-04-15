package de.teamnoco.books.test.e2e

import arrow.core.some
import com.trendyol.stove.testing.e2e.http.StoveMultiPartContent
import com.trendyol.stove.testing.e2e.http.http
import com.trendyol.stove.testing.e2e.system.TestSystem
import de.teamnoco.books.data.attachment.model.Attachment
import de.teamnoco.books.data.auth.dto.RegisterRequest
import de.teamnoco.books.data.auth.dto.SignInRequest
import de.teamnoco.books.data.book.dto.BookCreateRequest
import de.teamnoco.books.data.book.dto.BookModifyRequest
import de.teamnoco.books.data.book.enum.BookSize
import de.teamnoco.books.data.book.model.Book
import de.teamnoco.books.data.user.enum.UserRole
import de.teamnoco.books.data.user.model.User
import de.teamnoco.books.util.ZERO_UUID
import de.teamnoco.books.web.response.StatusResponse
import io.kotest.core.spec.Order
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource

@Order(5)
@SpringBootTest
class BookTest : StringSpec({
    val user = User(
        null,
        "evgeniy.kabarukhin2@gmail.com",
        UserRole.USER,
        "12345678",
        "Evgeniy Kabarukhin 2"
    )

    val user2 = User(
        null,
        "evgeniy.kabarukhin333@gmail.com",
        UserRole.ADMIN,
        "12345678",
        "Evgeniy Kabarukhin 333"
    )

    "test book creation and modify" {
        val testImageBytes = ClassPathResource("pomidor.jpeg").file.readBytes()

        TestSystem.validate {
            http {
                postAndExpectBodilessResponse(
                    "/api/auth/register", body = RegisterRequest(
                        user.email,
                        user.password,
                        user.name
                    ).some()
                ) {
                    it.status shouldBe 200
                }

                var session: String? = null
                postAndExpectBodilessResponse(
                    "/api/auth/sign-in",
                    body = SignInRequest(user.email, user.password).some()
                ) {
                    session = it.headers["set-cookie"]?.toString()?.split("; ")?.get(0)?.split("=")?.get(1)
                }

                var attachment: Attachment? = null
                postMultipartAndExpectResponse<Attachment>(
                    "/api/attachments",
                    listOf(StoveMultiPartContent.File("file", "nocode.png", testImageBytes, "image/png")),
                    headers = mapOf("Cookie" to "SESSION=$session")
                ) {
                    it.status shouldBe 201
                    attachment = it.body()
                }

                var book = Book(
                    ZERO_UUID,
                    "Приключения Девопсера",
                    "Евгений Кабарухин",
                    "1231231231231",
                    "Роман",
                    2025,
                    "Evgeniy Kabarukhin inc.",
                    "Русский",
                    Book.Cover.HARD,
                    251,
                    BookSize.MEDIUM,
                    attachment!!.id,
                    Book.Status.MODERATION
                )

                postAndExpectJson<Book>(
                    "/api/books", body = BookCreateRequest(
                        book.name,
                        book.author,
                        book.isbn,
                        book.genre,
                        book.editionYear,
                        book.publishingCompany,
                        book.language,
                        book.cover,
                        book.pages,
                        book.size,
                        book.coverId
                    ).some(), headers = mapOf("Cookie" to "SESSION=$session")
                ) {
                    it shouldBe book.copy(id = it.id)
                    book = it
                }

                patchAndExpectBody<StatusResponse>(
                    "/api/books/${book.id}", body = BookModifyRequest(
                        name = "Штильпобеда.рф"
                    ).some(), headers = mapOf("Cookie" to "SESSION=$session")
                ) {
                    it.status shouldBe 403 // нет админки
                }

                postAndExpectBodilessResponse(
                    "/api/auth/register", body = RegisterRequest(
                        user2.email,
                        user2.password,
                        user2.name
                    ).some()
                ) {
                    it.status shouldBe 200
                }
            }
        }
    }
})