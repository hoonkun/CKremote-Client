package kiwi.hoonkun.ckremote.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable


@Immutable
data class WindowMetadata(
    val width: Double,
    val height: Double
) {
    val ratio: Double = width / height
}

@Composable
expect fun rememberWindowMetadata(): WindowMetadata