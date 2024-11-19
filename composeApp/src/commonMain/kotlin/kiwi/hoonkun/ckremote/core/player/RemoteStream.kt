package kiwi.hoonkun.ckremote.core.player

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntSize

@Composable
expect fun NativeRemoteStream(
    state: CommunicatorState,
    remoteRatio: Double,
    underlayed: Boolean = false,
    coordinateMapping: RemoteCoordinateMapping,
    modifier: Modifier = Modifier
)

@Immutable
data class RemoteCoordinate(val x: Double, val y: Double)
typealias RemoteCoordinateMapping = (position: Offset, size: IntSize) -> RemoteCoordinate?

@Composable
fun RemoteStream(
    state: CommunicatorState,
    underlayed: Boolean = false,
    modifier: Modifier = Modifier
) {

    val remoteWindowRatio = state.remoteWindowRatio

    val animatedScale by animateFloatAsState(
        targetValue = if (underlayed) 1.2f else 1f,
        animationSpec = if (underlayed) tween(1000, easing = EaseOutCubic) else spring()
    )

    val mapping: RemoteCoordinateMapping = compute@ { position, size ->
        val clientRatio = size.width.toDouble() / size.height.toDouble()

        val widthRemains = remoteWindowRatio < clientRatio

        val (streamWidth, streamHeight) = if (widthRemains) {
            val w = remoteWindowRatio * size.height
            val h = (1 / remoteWindowRatio) * w
            w to h
        } else {
            val h = (1 / remoteWindowRatio) * size.width
            val w = remoteWindowRatio * h
            w to h
        }

        val unreachableAreaSize =
            if (widthRemains) (size.width - streamWidth) / 2
            else (size.height - streamHeight) / 2

        val x =
            if (widthRemains) { (position.x - unreachableAreaSize) / streamWidth }
            else { position.x / streamWidth }
        val y =
            if (widthRemains) { position.y / streamHeight }
            else { (position.y - unreachableAreaSize) / streamHeight }

//        val widthOverflows = remoteRatio > clientRatio
//
//        val streamWidth = remoteRatio * size.height
//        val streamHeight = (1.0 / remoteRatio) * size.width
//
//        val remainingWidth = if (widthOverflows) streamWidth - size.width else 0.0
//        val remainingHeight = if (widthOverflows) 0.0 else streamHeight - size.height
//
//        val x = (remainingWidth / 2 + change.position.x) / (if (widthOverflows) streamWidth else size.width.toDouble())
//        val y = (remainingHeight / 2 + change.position.y) / (if (widthOverflows) size.height.toDouble() else streamHeight)

        return@compute RemoteCoordinate(x.coerceIn(0.0, 1.0), y.coerceIn(0.0, 1.0))
    }

    NativeRemoteStream(
        state = state,
        remoteRatio = remoteWindowRatio,
        underlayed = underlayed,
        coordinateMapping = mapping,
        modifier = modifier
            .graphicsLayer { scaleX = animatedScale; scaleY = animatedScale }
    )

}