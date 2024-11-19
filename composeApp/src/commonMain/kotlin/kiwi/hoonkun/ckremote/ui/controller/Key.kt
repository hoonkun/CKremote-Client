package kiwi.hoonkun.ckremote.ui.controller

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.sp


sealed interface Key {
    val text: String
    val value: String
    val type: String
}

object Keys {
    val Arrows = listOf(0f, 90f, 180f, 270f)

    const val ArrowSize = 8

    const val ActionRowPadding = 32

    const val ControllerLineStroke = 2f

    const val ControllerActiveOffset = 12f
    const val ControllerInactiveOffset = 0f

    const val ControllerActiveAlpha = 0.7f
    const val ControllerInactiveAlpha = 0.2f

    @Composable
    fun animateOffset(pressed: Boolean) = animateFloatAsState(
        if (pressed) ControllerActiveOffset
        else ControllerInactiveOffset
    )

    @Composable
    fun animateAlpha(pressed: Boolean) = animateFloatAsState(
        if (pressed) ControllerActiveAlpha
        else ControllerInactiveAlpha
    )

    fun DrawScope.flipWidthIf(criteria: Boolean, width: Float) =
        if (criteria) size.width - width
        else width

    fun DrawScope.flipHeightIf(criteria: Boolean, height: Float) =
        if (criteria) size.height - height
        else height

    @Composable
    fun Label(
        key: Key,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = key.text,
            color = Color.White,
            fontSize = 14.sp,
            modifier = modifier
        )
    }

}

sealed interface HorizontalSymmetric {
    val right: Boolean
    val left: Boolean
}

sealed interface VerticalSymmetric {
    val top: Boolean
    val bottom: Boolean
}

sealed interface KeyState<T> {
    val value: T

    data object Down: KeyState<String> {
        override val value: String get() = "DOWN"
    }
    data object Up: KeyState<String> {
        override val value: String get() = "UP"
    }
    data class Analog(
        override val value: Int
    ): KeyState<Int>
}
