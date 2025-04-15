package de.teamnoco.books.test.e2e

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.SpecExecutionOrder

object KotestProjectConfig : AbstractProjectConfig() {
    override val specExecutionOrder: SpecExecutionOrder =
        SpecExecutionOrder.Annotated
}