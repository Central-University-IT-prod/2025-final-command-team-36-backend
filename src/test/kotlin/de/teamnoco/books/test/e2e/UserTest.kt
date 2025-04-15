package de.teamnoco.books.test.e2e

import arrow.core.some
import com.trendyol.stove.testing.e2e.http.http
import com.trendyol.stove.testing.e2e.system.TestSystem
import de.teamnoco.books.data.auth.dto.RegisterRequest
import de.teamnoco.books.data.auth.dto.SignInRequest
import de.teamnoco.books.data.user.dto.UserDto
import de.teamnoco.books.data.user.dto.UserUpdateRequest
import de.teamnoco.books.data.user.enum.UserRole
import de.teamnoco.books.data.user.model.User
import io.kotest.core.spec.Order
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest

@Order(3)
@SpringBootTest
class UserTest : StringSpec({
    val user = User(
        null,
        "evgeniy.kabarukhin123@gmail.com",
        UserRole.USER,
        "12345678",
        "Evgeniy Kabarukhin123"
    )

    "test user me get" {
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

                postAndExpectBodilessResponse(
                    "/api/auth/sign-in",
                    body = SignInRequest(user.email, user.password).some()
                ) {
                    it.status shouldBe 200
                    val session =
                        it.headers["set-cookie"]?.toString()?.split("; ")?.get(0)?.split("=")?.get(1) ?: error("")
                    get<UserDto>("/api/users/me", headers = mapOf("Cookie" to "SESSION=$session")) {
                        it.email shouldBe user.email
                    }
                }
            }
        }
    }

    "test user me patch" {
        TestSystem.validate {
            http {
                postAndExpectBodilessResponse(
                    "/api/auth/sign-in",
                    body = SignInRequest(user.email, user.password).some()
                ) {
                    val session =
                        it.headers["set-cookie"]?.toString()?.split("; ")?.get(0)?.split("=")?.get(1) ?: error("")
                    patchAndExpectJson<UserDto>(
                        "/api/users/me",
                        body = UserUpdateRequest("Schtil").some(),
                        headers = mapOf("Cookie" to "SESSION=$session")
                    ) {
                        it.name shouldBe "Schtil"
                    }
                }
            }
        }
    }

    "test user me delete" {
        TestSystem.validate {
            http {
                postAndExpectBodilessResponse(
                    "/api/auth/sign-in",
                    body = SignInRequest(user.email, user.password).some()
                ) {
                    val session =
                        it.headers["set-cookie"]?.toString()?.split("; ")?.get(0)?.split("=")?.get(1) ?: error("")
                    deleteAndExpectBodilessResponse(
                        "/api/users/me",
                        headers = mapOf("Cookie" to "SESSION=$session")
                    ) {
                        it.status shouldBe 204

                        getResponse("/api/users/me", headers = mapOf("Cookie" to "SESSION=$session")) {
                            it.status shouldNotBe 200
                        }
                    }
                }
            }
        }
    }
})