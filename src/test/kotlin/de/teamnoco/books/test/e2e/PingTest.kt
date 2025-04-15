package de.teamnoco.books.test.e2e

import com.redis.testcontainers.RedisContainer
import com.trendyol.stove.testing.e2e.http.http
import com.trendyol.stove.testing.e2e.system.TestSystem
import io.kotest.core.spec.Order
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.testcontainers.perProject
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName

@Order(1)
@SpringBootTest
class PingTest : StringSpec({

    listener(postgres.perProject())
    listener(redis.perProject())
    listener(s3.perProject())

    "test ping" {
        TestSystem.validate {
            http {
                getResponse("/api/ping") {
                    it.status shouldBe 200
                }
            }
        }
    }
}) {

    companion object {

        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:latest")
            .apply {
                start()
                System.setProperty("spring.datasource.url", jdbcUrl)
                System.setProperty("spring.datasource.username", username)
                System.setProperty("spring.datasource.password", password)
            }

        @ServiceConnection
        val redis = RedisContainer("redis:latest")
            .apply {
                start()
                System.setProperty("spring.data.redis.host", host)
                System.setProperty("spring.data.redis.port", redisPort.toString())
            }

        @ServiceConnection
        val s3 = LocalStackContainer(
            DockerImageName.parse("localstack/localstack:latest")
        )
            .withServices(LocalStackContainer.Service.S3)
            .apply {
                start()
                val endpoint = getEndpointOverride(LocalStackContainer.Service.S3).toString()
                execInContainer("/bin/bash", "-c", "awslocal s3 mb s3://test-bucket")
                System.setProperty(
                    "spring.cloud.aws.s3.endpoint",
                    endpoint
                )
                System.setProperty("spring.cloud.aws.s3.bucket", "test-bucket")
                System.setProperty("spring.cloud.aws.credentials.access-key", accessKey)
                System.setProperty("spring.cloud.aws.credentials.secret-key", secretKey)
                System.setProperty("spring.cloud.aws.region.static", "us-east-1")
            }


    }

}