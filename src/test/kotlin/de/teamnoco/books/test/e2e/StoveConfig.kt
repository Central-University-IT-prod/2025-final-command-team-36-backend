package de.teamnoco.books.test.e2e

import com.trendyol.stove.testing.e2e.http.HttpClientSystemOptions
import com.trendyol.stove.testing.e2e.http.httpClient
import com.trendyol.stove.testing.e2e.springBoot
import com.trendyol.stove.testing.e2e.system.TestSystem
import de.teamnoco.books.run
import io.kotest.core.config.AbstractProjectConfig

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
class StoveConfig : AbstractProjectConfig() {

    override suspend fun beforeProject(): Unit =
        TestSystem()
            .with {
                httpClient {
                    HttpClientSystemOptions(
                        baseUrl = "http://localhost:8080",
                    )
                }
                springBoot(
                    runner = { parameters ->
                        run(parameters)
                    },
                    withParameters = listOf(),
                )
            }.run()

    override suspend fun afterProject(): Unit = TestSystem.stop()

}