package kiwi.hoonkun.ckremote.core.player

import android.graphics.Bitmap
import android.view.PixelCopy
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kiwi.hoonkun.ckremote.utils.compose.getValue
import kiwi.hoonkun.ckremote.utils.compose.rememberMutableRefOf
import kiwi.hoonkun.ckremote.utils.compose.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.webrtc.MediaStream
import org.webrtc.SurfaceViewRenderer
import kotlin.coroutines.resume

@Composable
actual fun NativeRemoteStream(
    state: CommunicatorState,
    remoteRatio: Double,
    underlayed: Boolean,
    coordinateMapping: RemoteCoordinateMapping,
    modifier: Modifier
) {

    val scope = rememberCoroutineScope { Dispatchers.Main }

    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var view by rememberMutableRefOf<SurfaceViewRenderer?> { null }

    val previousStream = rememberMutableRefOf<MediaStream?> { null }
    val stream by state.remoteStream.flow.collectAsState()

    val cleanupPreviousStream: () -> Unit = {
        previousStream.value?.firstVideoTrack()?.removeSink(view)
    }

    val connectStream: (MediaStream?) -> Unit = {
        val videoTrack = stream?.firstVideoTrack()
        val audioTrack = stream?.firstAudioTrack()

        videoTrack?.setEnabled(true)
        audioTrack?.setEnabled(true)

        videoTrack?.addSink(view)

        previousStream.value = stream
    }

    DisposableEffect(underlayed) {
        if (!underlayed) return@DisposableEffect onDispose {  }

        val job = scope.launch {
            bitmap = suspendCancellableCoroutine { continuation ->
                val capturedView = view ?: return@suspendCancellableCoroutine continuation.resume(null)
                val newBitmap = Bitmap.createBitmap(capturedView.width, capturedView.height, Bitmap.Config.ARGB_8888)
                val listener = PixelCopy.OnPixelCopyFinishedListener { continuation.resume(newBitmap.asImageBitmap()) }
                PixelCopy.request(capturedView, newBitmap, listener, capturedView.handler)
            }
        }

        onDispose { job.cancel() }
    }

    DisposableEffect(stream) {
        connectStream(stream)
        onDispose { cleanupPreviousStream() }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        AndroidView(
            factory = { context ->
                SurfaceViewRenderer(context)
                    .apply {
                        init(state.remoteStream.eglBase.eglBaseContext, null)
                    }
                    .also { view = it }
            },
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(state.remoteWindowRatio.toFloat())
                .pointerInput(true) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            when (event.type) {
                                PointerEventType.Press -> {
                                    val position =
                                        currentEvent.changes.firstOrNull()?.position ?: continue
                                    val (x, y) = coordinateMapping(position, size) ?: continue
                                    state.emulateKey("MSE LEFT DOWN $x $y")
                                }

                                PointerEventType.Move -> {
                                    val position =
                                        currentEvent.changes.firstOrNull()?.position ?: continue
                                    val (x, y) = coordinateMapping(position, size) ?: continue
                                    state.emulateKey("MSE _ MOVE $x $y")
                                }

                                PointerEventType.Release -> {
                                    state.emulateKey("MSE LEFT UP")
                                }
                            }
                        }
                    }
                }
        )

        AnimatedVisibility(
            visible = underlayed,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            bitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(state.remoteWindowRatio.toFloat())
                        .blur(30.dp)
                )
            }
        }
    }

}

fun MediaStream.firstVideoTrack() = videoTracks.firstOrNull()
fun MediaStream.firstAudioTrack() = audioTracks.firstOrNull()
