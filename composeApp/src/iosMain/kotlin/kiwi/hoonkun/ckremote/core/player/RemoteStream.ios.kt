package kiwi.hoonkun.ckremote.core.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.viewinterop.UIKitView
import cocoapods.WebRTC.RTCMTLVideoView
import cocoapods.WebRTC.RTCMediaStream
import cocoapods.WebRTC.RTCVideoTrack
import kiwi.hoonkun.ckremote.utils.compose.rememberMutableRefOf
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.QuartzCore.kCAFilterNearest
import platform.UIKit.UIBlurEffect
import platform.UIKit.UIBlurEffectStyle
import platform.UIKit.UIViewAnimationCurve
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewContentMode
import platform.UIKit.UIViewPropertyAnimator
import platform.UIKit.UIVisualEffect
import platform.UIKit.UIVisualEffectView

@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
actual fun NativeRemoteStream(
    state: CommunicatorState,
    remoteRatio: Double,
    underlayed: Boolean,
    coordinateMapping: RemoteCoordinateMapping,
    modifier: Modifier
) {

    val effect = remember {
        IntensityUIVisualEffectView(
            effect = UIBlurEffect.effectWithStyle(UIBlurEffectStyle.UIBlurEffectStyleDark),
            enabled = underlayed
        )
    }

    val view = remember {
        RTCMTLVideoView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0))
            .apply {
                autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
                layer.allowsEdgeAntialiasing = false
                layer.minificationFilter = kCAFilterNearest
                layer.magnificationFilter = kCAFilterNearest

                addSubview(effect)
            }
    }

    val previousStream = rememberMutableRefOf<RTCMediaStream?> { null }
    val stream by state.remoteStream.flow.collectAsState()

    val cleanupPreviousStream: () -> Unit = {
        previousStream.value?.firstVideoTrack()?.removeRenderer(view)
    }

    val connectStream: (RTCMediaStream?) -> Unit = {
        view.videoContentMode = UIViewContentMode.UIViewContentModeScaleAspectFit

        val videoTrack = stream?.firstVideoTrack()
        val audioTrack = stream?.firstAudioTrack()

        videoTrack?.setIsEnabled(true)
        audioTrack?.setIsEnabled(true)
        videoTrack?.addRenderer(view)

        previousStream.value = stream
    }

    DisposableEffect(stream) {
        connectStream(stream)
        onDispose { cleanupPreviousStream() }
    }

    LaunchedEffect(underlayed) {
        effect.setFrame(view.frame.useContents { CGRectMake(origin.x, origin.y, size.width * 1.2, size.height * 1.2) })
        effect.enabled = underlayed
    }

    UIKitView(
        factory = { view },
        onRelease = { cleanupPreviousStream() },
        modifier = modifier
            .onPointerEvent(PointerEventType.Press) {
                val position = currentEvent.changes.firstOrNull()?.position ?: return@onPointerEvent
                val (x, y) = coordinateMapping(position, size) ?: return@onPointerEvent
                state.emulateKey("MSE LEFT DOWN $x $y")
            }
            .onPointerEvent(PointerEventType.Move) {
                val position = currentEvent.changes.firstOrNull()?.position ?: return@onPointerEvent
                val (x, y) = coordinateMapping(position, size) ?: return@onPointerEvent
                state.emulateKey("MSE _ MOVE $x $y")
            }
            .onPointerEvent(PointerEventType.Release) {
                state.emulateKey("MSE LEFT UP")
            }
    )
}

@OptIn(ExperimentalForeignApi::class)
fun RTCMediaStream.firstVideoTrack() = videoTracks
    .filterIsInstance<RTCVideoTrack>()
    .firstOrNull()

@OptIn(ExperimentalForeignApi::class)
fun RTCMediaStream.firstAudioTrack() = audioTracks
    .filterIsInstance<RTCVideoTrack>()
    .firstOrNull()


class IntensityUIVisualEffectView(
    effect: UIVisualEffect,
    enabled: Boolean
) : UIVisualEffectView(effect = null) {

    var enabled = enabled
        set(value) {
            field = value
            blurAnimator.reversed = !value
            blurAnimator.startAnimation()
        }

    private val blurAnimator = UIViewPropertyAnimator(
        duration = 0.3,
        curve = UIViewAnimationCurve.UIViewAnimationCurveLinear,
        animations = { this.effect = effect }
    ).apply {
        reversed = !enabled
        fractionComplete = 1.0
        pausesOnCompletion = true
    }

}
