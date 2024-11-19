package kiwi.hoonkun.ckremote.utils.compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.max


@Composable
fun Modifier.horizontalSafeAreaPadding(): Modifier {
    val (start, end) = with(LocalDensity.current) {
        val left = WindowInsets.safeContent.getLeft(this, LayoutDirection.Ltr).toDp()
        val right = WindowInsets.safeContent.getRight(this, LayoutDirection.Ltr).toDp()

        left to right
    }

    return padding(
        start = max(start, end),
        end = max(start, end)
    )
}

fun Modifier.offset(x: Float = 0f, y: Float = 0f) =
    layout { measurable, constraint ->
        val placeable = measurable.measure(constraint)

        layout(placeable.width, placeable.height) {
            placeable.placeRelative(
                x = (placeable.width * x).toInt(),
                y = (placeable.height * y).toInt()
            )
        }
    }
