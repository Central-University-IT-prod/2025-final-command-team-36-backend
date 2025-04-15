package de.teamnoco.books.test.e2e

import arrow.core.some
import com.trendyol.stove.testing.e2e.http.StoveMultiPartContent
import com.trendyol.stove.testing.e2e.http.http
import com.trendyol.stove.testing.e2e.system.TestSystem
import de.teamnoco.books.data.attachment.model.Attachment
import de.teamnoco.books.data.auth.dto.RegisterRequest
import de.teamnoco.books.data.auth.dto.SignInRequest
import de.teamnoco.books.data.user.dto.UserDto
import de.teamnoco.books.data.user.enum.UserRole
import de.teamnoco.books.data.user.model.User
import io.kotest.core.spec.Order
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.core.io.ClassPathResource

@Order(4)
class AttachmentTest : StringSpec({
    val user = User(
        null,
        "evgeniy.kabarukhin1234@gmail.com",
        UserRole.USER,
        "12345678",
        "Evgeniy Kabarukhin1234"
    )

    val testImageBytes = ClassPathResource("pomidor.jpeg").file.readBytes()
    "should upload attachment" {
        TestSystem.validate {
            http {
                postAndExpectJson<UserDto>(
                    "/api/auth/register", body = RegisterRequest(
                        user.email,
                        user.password,
                        user.name
                    ).some()
                ) { actualUser ->
                    actualUser.name shouldBe user.name
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

                get<Attachment>("/api/attachments/${attachment!!.id}") { actual ->
                    actual shouldBe attachment
                }

                getResponse<Any>("/api/attachments/${attachment!!.id}/content") { actual ->
                    actual.body.shouldNotBeNull {}
                    actual.status shouldBe 200
                    actual.headers.keys shouldContain "content-disposition"
                }
            }
        }
    }
})