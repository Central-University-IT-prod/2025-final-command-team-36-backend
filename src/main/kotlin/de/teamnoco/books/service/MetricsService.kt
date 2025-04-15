package de.teamnoco.books.service

import de.teamnoco.books.data.book.repo.BookRepository
import de.teamnoco.books.data.instance.repository.BookInstanceRepository
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service

@Service
class MetricsService(
    meterRegistry: MeterRegistry,
    private val bookRepository: BookRepository,
    private val userRepository: BookRepository,
    private val bookInstanceRepository: BookInstanceRepository
) {
    init {
        Gauge.builder("books_count") { bookRepository.count() }
            .description("Books count").register(meterRegistry)
        Gauge.builder("user_count") { userRepository.count() }
            .description("User count").register(meterRegistry)
        Gauge.builder("book_instance_count") { bookInstanceRepository.count() }
            .description("Book instances count").register(meterRegistry)
    }
}