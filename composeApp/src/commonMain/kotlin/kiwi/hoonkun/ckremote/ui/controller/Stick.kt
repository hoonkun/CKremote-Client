package kiwi.hoonkun.ckremote.ui.controller

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.minus
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toSize
import kiwi.hoonkun.ckremote.utils.compose.angle
import kiwi.hoonkun.ckremote.utils.compose.coerceInDistance
import kiwi.hoonkun.ckremote.utils.compose.getValue
import kiwi.hoonkun.ckremote.utils.compose.nullIndicationClickable
import kiwi.hoonkun.ckremote.utils.compose.rememberMutableRefOf
import kiwi.hoonkun.ckremote.utils.compose.setValue
import kotlinx.datetime.Clock


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Stick(
    type: Stick,
    onPull: (Stick, StickState, Boolean, Boolean) -> Unit,
    onClick: () -> Unit = { },
    edgeDistance: Dp = Dp.Unspecified,
    modifier: Modifier = Modifier,
    overlay: DrawScope.(() -> Float) -> Unit = { }
) {

    val density = LocalDensity.current

    var stickHandlingOffset by rememberMutableRefOf<Offset?> { null }
    var stickDisplayOffset by remember { mutableStateOf<Offset?>(null) }

    var dragStartAt by rememberMutableRefOf { -1L }

    var inEdge by remember { mutableStateOf(false) }

    var fieldSize by rememberMutableRefOf { IntSize.Zero }

    val interaction = remember { MutableInteractionSource() }

    val alphaState by Keys.animateAlpha(stickDisplayOffset != null)
    val edgeAlphaMultiplier by animateFloatAsState(if (inEdge || edgeDistance == Dp.Unspecified) 1f else 0.2f)

    val animatedStickOffset by animateOffsetAsState(stickDisplayOffset ?: Offset.Zero)

    val onPress = { offset: Offset ->
        val newOffset = (stickHandlingOffset ?: Offset.Zero) + offset

        val maxDistance = (fieldSize.toSize().minDimension - with(density) { 64.dp.toPx() }) / 2

        val centeredOffset = (newOffset - fieldSize.center)
            .coerceInDistance(maxDistance)

        val edgeInPrevious = inEdge
        val edgeInCurrent = centeredOffset.getDistance() > maxDistance - with(density) { edgeDistance.toPx() }

        onPull(type, centeredOffset.toStickState(maxDistance), edgeInPrevious, edgeInCurrent)

        stickHandlingOffset = newOffset
        stickDisplayOffset = centeredOffset
        inEdge = edgeInCurrent
    }

    val onRelease = {
        stickHandlingOffset = null
        stickDisplayOffset = null

        onPull(type, StickState(0f, 0f), inEdge, false)

        inEdge = false
    }

    val draggable = rememberDraggable2DState(onPress)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .onGloballyPositioned {
                fieldSize = it.size
            }
            .draggable2D(
                state = draggable,
                interactionSource = interaction,
                startDragImmediately = true,
                onDragStarted = { dragStartAt = Clock.System.now().toEpochMilliseconds(); onPress(it) },
                onDragStopped = {
                    if (Clock.System.now().toEpochMilliseconds() - dragStartAt < 50) {
                        onClick()
                    }
                    dragStartAt = -1
                    onRelease()
                }
            )
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .padding(24.dp)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            val baseAlpha = (alphaState - Keys.ControllerInactiveAlpha) * 0.5f
            drawCircle(
                color = Color.White,
                radius = (size.minDimension - 12.dp.toPx()) / 2f,
                alpha = baseAlpha * edgeAlphaMultiplier,
                style = Stroke(1.dp.toPx())
            )
            if (edgeDistance != Dp.Unspecified) {
                drawCircle(
                    Color.White,
                    radius = (size.minDimension - 12.dp.toPx() - edgeDistance.toPx()) / 2.0f,
                    alpha = baseAlpha * (1f - edgeAlphaMultiplier + 0.2f),
                    style = Stroke(1.dp.toPx())
                )
            }
            overlay { alphaState }
        }

        Canvas(
            modifier = Modifier
                .size(40.dp)
                .offset { animatedStickOffset.round() }
                .nullIndicationClickable { onClick() }
        ) {
            drawCircle(color = Color.White, alpha = alphaState)
            drawCircle(color = Color.White, alpha = alphaState, radius = size.minDimension / 2.0f - 4.dp.toPx())
        }
    }

}

fun DrawScope.DPadStickUnderlay(alphaRetriever: () -> Float) {
    Keys.Arrows.forEach {
        rotate(it) {
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(size.width / 2f - Keys.ArrowSize.dp.toPx() - 6.dp.toPx(), 0f),
                size = Size((Keys.ArrowSize.dp.toPx() + 6.dp.toPx()) * 2, size.height),
                blendMode = BlendMode.Clear
            )
        }
    }
    Keys.Arrows.forEach {
        rotate(it) {
            drawPath(
                path = Path().apply {
                    moveTo(size.width / 2f - Keys.ArrowSize.dp.toPx(), Keys.ArrowSize.dp.toPx())
                    lineTo(size.width / 2f, 0f)
                    lineTo(size.width / 2f + Keys.ArrowSize.dp.toPx(), Keys.ArrowSize.dp.toPx())
                },
                color = Color.White,
                alpha = alphaRetriever(),
                style = Stroke(Keys.ControllerLineStroke.dp.toPx())
            )
        }
    }
}


enum class Stick(
    val text: String,
    val value: String
) {
    Left("L", "LEFT"), Right("R", "RIGHT")
}

data class StickState(
    val angle: Float,
    val distance: Float
)

private fun Offset.toStickState(maxDistance: Float) =
    StickState(
        angle = angle,
        distance = (getDistance() / maxDistance).coerceAtMost(1.0f)
    )
