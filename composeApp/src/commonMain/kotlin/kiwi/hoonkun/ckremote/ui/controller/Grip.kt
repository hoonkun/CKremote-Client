package kiwi.hoonkun.ckremote.ui.controller

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kiwi.hoonkun.ckremote.ui.controller.Keys.flipWidthIf


@Composable
fun Grip(
    type: Grip,
    onDown: (Grip) -> Unit,
    onUp: (Grip) -> Unit,
    modifier: Modifier = Modifier
) {

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val alphaState by Keys.animateAlpha(pressed)
    val offsetState by Keys.animateOffset(pressed)

    LaunchedEffect(pressed) {
        if (pressed) onDown(type)
        else onUp(type)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(80.dp, 50.dp)
            .clickable(interaction, null) { /* noop */ }
            .padding(vertical = 8.dp)
            .offset { IntOffset(x = offsetState.dp.roundToPx() * if (type.right) -1 else 1, y = 0) }
            .drawBehind {
                drawPath(
                    path = Path().apply {
                        moveTo(x = flipWidthIf(type.right, size.width - 10.dp.toPx()), y = 0f)
                        lineTo(x = flipWidthIf(type.right, size.width), y = 0f)
                        lineTo(x = flipWidthIf(type.right, size.width), y = size.height)
                        lineTo(x = flipWidthIf(type.right, size.width * 0.6f), y = size.height)
                    },
                    color = Color.White,
                    alpha = alphaState,
                    style = Stroke(Keys.ControllerLineStroke.dp.toPx())
                )
            }
    ) {
        Keys.Label(
            key = type,
            modifier = Modifier.graphicsLayer { alpha = alphaState }
        )
    }
}

enum class Grip(
    override val text: String,
    override val value: String,
): Key, HorizontalSymmetric {
    Left1("L4", "Q"),
    Left2("L5", "B"),
    Right1("R4", "N"),
    Right2("R5", "M");

    override val left get() = this == Left1 || this == Left2
    override val right get() = this == Right1 || this == Right2

    override val type: String get() = "KEY"
}