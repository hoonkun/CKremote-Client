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
import kiwi.hoonkun.ckremote.ui.controller.Keys.flipHeightIf


@Composable
fun Action(
    type: Action,
    onDown: (Action) -> Unit,
    onUp: (Action) -> Unit,
    modifier: Modifier = Modifier.size(70.dp, 65.dp)
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
            .clickable(interaction, null) { /* noop */ }
            .padding(top = 12.dp)
            .padding(horizontal = 8.dp)
            .offset { IntOffset(x = 0, y = offsetState.dp.roundToPx() * if (type.bottom) -1 else 1) }
            .drawBehind {
                drawPath(
                    path = Path().apply {
                        moveTo(x = 0f, y = flipHeightIf(type.top, 0f))
                        lineTo(x = size.width, y = flipHeightIf(type.top, 0f))
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

enum class Action(
    override val text: String,
    override val value: String = text,
): Key, VerticalSymmetric {
    A("A"),
    B("B"),
    X("X"),
    Y("Y"),
    Menu("MENU"),
    Select("SELECT", "START");

    override val bottom get() = this == A || this == B || this == X || this == Y
    override val top get() = this == Menu || this == Select

    override val type: String get() = "BTN"
}
