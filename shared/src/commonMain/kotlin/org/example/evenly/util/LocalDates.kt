package org.example.evenly.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

object LocalDates {
    fun todayUtcDateMillis(): Long = toUtcDateMillis(Clock.System.now())

    fun toUtcDateMillis(instant: Instant): Long = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

    fun withDate(instant: Instant, utcDateMillis: Long): Instant {
        val zone = TimeZone.currentSystemDefault()
        val date = Instant.fromEpochMilliseconds(utcDateMillis).toLocalDateTime(TimeZone.UTC).date
        val time = instant.toLocalDateTime(zone).time
        return LocalDateTime(date, time).toInstant(zone)
    }
}
