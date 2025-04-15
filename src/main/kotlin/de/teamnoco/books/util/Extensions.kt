package de.teamnoco.books.util

import java.time.Instant
import java.time.temporal.ChronoField
import java.util.*
import kotlin.random.asKotlinRandom
import kotlin.time.Duration

val ZERO_UUID = UUID(0, 0)

fun Instant.zeroMillis(): Instant = minusMillis(getLong(ChronoField.MILLI_OF_SECOND))

operator fun Instant.plus(duration: Duration): Instant = plusMillis(duration.inWholeMilliseconds)

operator fun Date.compareTo(instant: Instant) = compareTo(Date.from(instant))

fun <T> Collection<T>.random(random: Random): T = this.random(random.asKotlinRandom())
