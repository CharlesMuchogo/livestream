package com.charlesmuchogo.livestream.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.Instant

fun Instant.clockTime(zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val time = toLocalDateTime(zone).time
    return time.hour.toString().padStart(2, '0') + ":" + time.minute.toString().padStart(2, '0')
}

fun Instant.untilLabel(now: Instant = Clock.System.now()): String {
    val minutes = (this - now).inWholeMinutes
    return when {
        minutes <= 0 -> "Starting"
        minutes < 60 -> "in ${minutes}m"
        minutes < 24 * 60 -> {
            val hours = minutes / 60
            val rest = minutes % 60
            if (rest == 0L || hours >= 3) "in ${hours}h" else "in ${hours}h ${rest}m"
        }
        else -> "in ${minutes / (24 * 60)}d"
    }
}

fun Instant.dayLabel(zone: TimeZone = TimeZone.currentSystemDefault(), now: Instant = Clock.System.now()): String {
    val day = toLocalDateTime(zone).date
    val today = now.toLocalDateTime(zone).date
    return when (day) {
        today -> "Today"
        LocalDate.fromEpochDays(today.toEpochDays() + 1) -> "Tomorrow"
        else -> day.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() } + " " + day.day
    }
}

fun scheduleDayParts(isoDate: String, zone: TimeZone = TimeZone.currentSystemDefault()): Pair<String, String> {
    val date = LocalDate.parse(isoDate)
    val today = Clock.System.todayIn(zone)
    val weekday = when (date) {
        today -> "Today"
        LocalDate.fromEpochDays(today.toEpochDays() + 1) -> "Tmrw"
        else -> date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    }
    return weekday to date.day.toString()
}

fun initials(name: String): String {
    val words = name.filter { it.isLetter() || it == ' ' }.split(' ').filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words[0].take(3).uppercase()
        else -> (words.first().first().toString() + words.last().first()).uppercase()
    }
}
