package kiwi.hoonkun.ckremote.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime


@Composable
fun rememberFormattedDate(epochSeconds: Long) = remember(epochSeconds) {
    val format = LocalDateTime.Format {
        year()
        char('.')
        monthNumber()
        char('.')
        dayOfMonth()
        char(' ')
        hour()
        char(':')
        minute()
    }
    Instant.fromEpochSeconds(epochSeconds)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .format(format)
}