package de.teamnoco.books

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext

@SpringBootApplication
class BooksApplication

fun main(args: Array<String>) {
    runApplication<BooksApplication>(*args)
}

fun run(
    args: Array<String>,
    init: SpringApplication.() -> Unit = {},
): ConfigurableApplicationContext {
    return runApplication<BooksApplication>(*args, init = init)
}
