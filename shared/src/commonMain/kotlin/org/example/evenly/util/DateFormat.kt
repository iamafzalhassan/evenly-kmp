package org.example.evenly.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

object DateFormat {
    fun formatDateTime(instant: Instant): String {
        val time = instant.toLocalDateTime(TimeZone.currentSystemDefault()).time
        return "${formatShort(instant)}, ${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
    }

    fun formatShort(instant: Instant): String {
        val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return "${date.day} ${MonthNames.ENGLISH_ABBREVIATED.names[date.month.ordinal]} ${date.year}"
    }
}
