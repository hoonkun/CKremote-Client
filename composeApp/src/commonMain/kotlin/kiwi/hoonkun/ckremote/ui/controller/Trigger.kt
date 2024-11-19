package kiwi.hoonkun.ckremote.ui.controller

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
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
import androidx.compose.ui.unit.sp
import kiwi.hoonkun.ckremote.ui.controller.Keys.flipWidthIf


@Composable
fun Trigger(
    type: Trigger,
    onAnalog: (Trigger, Int) -> Unit,
    stickActivate: Boolean = false,
    modifier: Modifier = Modifier
) {

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val alphaState by Keys.animateAlpha(pressed)
    val offsetState by Keys.animateOffset(pressed)

    LaunchedEffect(pressed) {
        if (pressed) onAnalog(type, 256)
        else onAnalog(type, 0)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(120.dp, 50.dp)
            .clickable(interaction, null) { /* noop */ }
            .padding(bottom = 12.dp)
            .offset { IntOffset(x = offsetState.dp.roundToPx() * if (type.right) -1 else 1, y = 0) }
            .drawBehind {
                drawPath(
                    path = Path().apply {
                        moveTo(x = flipWidthIf(type.right, size.width), y = size.height * 0.7f)
                        lineTo(x = flipWidthIf(type.right, size.width * 0.7f), y = size.height * 0.7f)
                        lineTo(x = flipWidthIf(type.right, size.width * 0.7f), y = size.height)
                        lineTo(x = flipWidthIf(type.right, 0f), y = size.height)
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
        if (stickActivate) {
            Text(
                text = "STICK",
                fontSize = 10.sp,
                modifier = Modifier
                    .offset(x = 30.dp * if (type.right) 1 else -1, y = (-10).dp)
                    .graphicsLayer { alpha = alphaState }
            )
        }
    }
}

enum class Trigger(
    override val text: String,
    override val value: String,
): Key, HorizontalSymmetric {
    Left("LT", "LEFT"), Right("RT", "RIGHT");

    override val right get() = this == Right
    override val left get() = this == Left

    override val type: String get() = "TRG"
}