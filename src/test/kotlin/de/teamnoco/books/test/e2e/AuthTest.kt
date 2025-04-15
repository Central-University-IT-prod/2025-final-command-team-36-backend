package de.teamnoco.books.test.e2e

import arrow.core.None
import arrow.core.some
import com.trendyol.stove.testing.e2e.http.http
import com.trendyol.stove.testing.e2e.system.TestSystem
import de.teamnoco.books.data.auth.dto.RegisterRequest
import de.teamnoco.books.data.auth.dto.SignInRequest
import de.teamnoco.books.data.user.dto.UserDto
import de.teamnoco.books.data.user.enum.UserRole
import de.teamnoco.books.data.user.model.User
import io.kotest.core.spec.Order
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest

@Order(2)
@SpringBootTest
class AuthTest : StringSpec({
    val user = User(
        null,
        "evgeniy.kabarukhin@gmail.com",
        UserRole.USER,
        "12345678",
        "Evgeniy Kabarukhin"
    )

    "test user register" {
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
            }
        }
    }

    "test user register conflict" {
        TestSystem.validate {
            http {
                postAndExpectBodilessResponse(
                    "/api/auth/register", body = RegisterRequest(
                        user.email,
                        user.password,
                        user.name
                    ).some()
                ) {
                    it.status shouldBe 409
                }
            }
        }
    }

    "test user sign in bad credentials" {
        TestSystem.validate {
            http {
                postAndExpectBodilessResponse(
                    "/api/auth/sign-in",
                    body = SignInRequest(user.email, "12341234").some()
                ) {
                    it.status shouldBe 401
                }
            }
        }
    }

    "test user sign in ok" {
        TestSystem.validate {
            http {
                postAndExpectJson<UserDto>(
                    "/api/auth/sign-in",
                    body = SignInRequest(user.email, user.password).some()
                ) {
                    it.email shouldBe "evgeniy.kabarukhin@gmail.com"
                }
            }
        }
    }

    "test user log out ok" {
        TestSystem.validate {
            http {
                postAndExpectBodilessResponse(
                    "/api/auth/sign-in",
                    body = SignInRequest(user.email, user.password).some()
                ) {
                    val session = it.headers["set-cookie"]?.toString()?.split("; ")?.get(0)?.split("=")?.get(1)
                    postAndExpectBodilessResponse(
                        "/api/auth/log-out",
                        body = None,
                        headers = mapOf("Cookie" to "SESSION=$session")
                    ) {
                        it.status shouldBe 204
                    }
                }
            }
        }
    }
})