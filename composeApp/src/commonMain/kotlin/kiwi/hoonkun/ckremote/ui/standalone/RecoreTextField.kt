package kiwi.hoonkun.ckremote.ui.standalone

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp


private val ThemeColor = Color(0xff326fa8)
private val ErrorColor = Color(0xffa84832)
private val DrawScope.Style get() = Stroke(width = 1.dp.toPx())

@Composable
fun Modifier.ckTextFieldStyle(
    interactionSource: MutableInteractionSource,
    hasErrors: Boolean = false
): Modifier {
    val focused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        if (hasErrors) ErrorColor else if (focused) ThemeColor else Color.White.copy(alpha = 0.3f)
    )
    val backgroundColor by animateColorAsState(
        if (hasErrors) ErrorColor else if (focused) ThemeColor else Color.Black.copy(alpha = 0.3f)
    )
    val backgroundAlpha by animateFloatAsState(
        if (focused) 0f else 0.2f
    )

    return this
        .drawBehind {
            drawRect(borderColor, style = Style)
            drawRect(
                Brush.horizontalGradient(
                    0f to backgroundColor.copy(alpha = backgroundAlpha),
                    0.6f to backgroundColor.copy(alpha = backgroundAlpha),
                    1f to backgroundColor.copy(alpha = 0.2f),
                )
            )
        }
        .padding(horizontal = 12.dp, vertical = 12.dp)
}