package kiwi.hoonkun.ckremote.utils.compose

import androidx.compose.ui.graphics.Color


fun Color.grayscale(intensity: Float = 1f) = ((red + green + blue) / 3f).let {
    Color(
        red = (1 - intensity) * red + intensity * it,
        green = (1 - intensity) * green + intensity * it,
        blue = (1 - intensity) * blue + intensity * it,
        alpha = alpha
    )
}