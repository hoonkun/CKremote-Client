package kiwi.hoonkun.ckremote.core.player

import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLExt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import kiwi.hoonkun.ckremote.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.RtpTransceiver.RtpTransceiverInit
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import java.nio.ByteBuffer
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


actual class Communicator actual constructor(
    currentServer: RemoteServer
): SignalingCommunicator, RtcCommunicator {

    actual var remoteRatio: Double by mutableDoubleStateOf(1.0)

    override val server: RemoteServer = currentServer

    override val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    override val client: HttpClient = buildCommunicatorClient()

    override var connectionJob: Job? = null
    override var socketSession: ClientWebSocketSession? = null

    override val stateFlow: MutableStateFlow<State> = MutableStateFlow(State.Unspecified)
    override val errorFlow: MutableStateFlow<Error?> = MutableStateFlow(null)

    private val peerObserver = PeerConnectionObserver()
    private val remotePeer = buildPeerConnection(peerObserver)
        ?: throw RuntimeException("Assertion Failed: remotePeer is null")
    private var remoteChannel: DataChannel? = null
    private val remoteStream: MutableStateFlow<MediaStream?> = MutableStateFlow(null)

    override fun handleInit(frame: RemoteFrame.InitResponse) {
        super.handleInit(frame)
        remoteRatio = frame.screenRatio
    }

    override suspend fun handleOffer(frame: RemoteFrame.RtcOffer) {
        super.handleOffer(frame)
        dispatchServerOffer(frame)
    }

    override suspend fun handleCandidate(frame: RemoteFrame.RtcCandidate) {
        dispatchServerCandidate(frame)
    }

    override suspend fun dispatchServerOffer(frame: RemoteFrame.RtcOffer) {
        remotePeer.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO, buildRecvOnlyRtcRtpTransceiverInit())
        remotePeer.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO, buildRecvOnlyRtcRtpTransceiverInit())

        remotePeer.setRemoteDescription(frame.toSessionDescription())

        val answer = remotePeer.createAnswer()

        remotePeer.setLocalDescription(answer)
        writeSocket(RemoteFrame.RtcAnswer(answer.description))
    }

    override suspend fun dispatchServerCandidate(frame: RemoteFrame.RtcCandidate) {
        remotePeer.addIceCandidate(frame.toIceCandidate())
    }

    override fun writeChannel(payload: String) {
        val remoteChannel = remoteChannel ?: return
        remoteChannel.send(DataChannel.Buffer(ByteBuffer.wrap(payload.toByteArray()), false))
    }

    override fun closeRtcElements() {
        remotePeer.close()
        remoteChannel?.close()
    }

    actual interface State {
        actual val description: String
        actual val completed: Boolean

        actual data object Unspecified : State {
            override val description: String = ""
            override val completed: Boolean = false
        }
    }

    actual interface Error {
        actual val description: String
    }

    inner class PeerConnectionObserver: PeerConnection.Observer {

        override fun onAddStream(p0: MediaStream?) {
            coroutineScope.launch {
                while (!stateFlow.value.completed) {
                    yield()
                }
                remoteStream.value = p0
            }
        }

        override fun onIceCandidate(p0: IceCandidate?) {
            val candidate = p0 ?: return
            coroutineScope.launch {
                writeSocket(candidate.toRemoteFrame())
            }
        }

        override fun onDataChannel(p0: DataChannel?) {
            remoteChannel = p0
        }

        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
            when (p0) {
                PeerConnection.IceConnectionState.CHECKING ->
                    stateFlow.value = RtcCommunicator.State.IceStateChecking
                PeerConnection.IceConnectionState.CONNECTED ->
                    stateFlow.value = RtcCommunicator.State.IceStateConnected
                else -> { /* noop */ }
            }
        }

        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
            when (p0) {
                PeerConnection.IceGatheringState.GATHERING ->
                    stateFlow.value = RtcCommunicator.State.IceGathering
                PeerConnection.IceGatheringState.COMPLETE ->
                    stateFlow.value = RtcCommunicator.State.IceGathered
                else -> { /* noop */ }
            }
        }

        override fun onRemoveStream(p0: MediaStream?) { }
        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) { }
        override fun onSignalingChange(p0: PeerConnection.SignalingState?) { }
        override fun onRenegotiationNeeded() { }
        override fun onIceConnectionReceivingChange(p0: Boolean) { }

    }

    actual class RemoteStream actual constructor(private val communicator: Communicator) {
        val flow get() = communicator.remoteStream
        val eglBase get() = Communicator.eglBase
    }

    companion object {
        private val eglBase: EglBase = EglBase.createEgl14(intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR,
            EGLExt.EGL_RECORDABLE_ANDROID,
            1,
            EGL14.EGL_NONE
        ))

        private val videoDecoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

        val ConnectionFactory: PeerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options())
            .setVideoDecoderFactory(videoDecoderFactory)
            .createPeerConnectionFactory()
    }

}

fun initializeWebRTC(context: Context) {
    System.loadLibrary("jingle_peerconnection_so")

    PeerConnectionFactory.initialize(
        PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
    )
}

private fun createIceServer(uri: String, credential: Pair<String, String>? = null): PeerConnection.IceServer {
    var builder = PeerConnection.IceServer.builder(uri)

    credential?.let {
        val (username, password) = it
        builder = builder
            .setUsername(username)
            .setPassword(password)
    }

    return builder.createIceServer()
}

private fun buildPeerConnection(observer: PeerConnection.Observer): PeerConnection? =
    Communicator.ConnectionFactory.createPeerConnection(
        PeerConnection.RTCConfiguration(emptyList()).apply {
            iceServers = listOf(
                createIceServer(
                    uri = Constants.TurnServer.URI,
                    credential = Constants.TurnServer.USERNAME to Constants.TurnServer.PASSWORD
                )
            )
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            disableIPv6OnWifi = false
        },
        observer
    )

private fun buildRecvOnlyRtcRtpTransceiverInit(): RtpTransceiverInit =
    RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)

private fun createSdpSetObserver(continuation: Continuation<Unit>) =
    object: SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) { }

        override fun onSetSuccess() { continuation.resume(Unit) }

        override fun onCreateFailure(p0: String?) { }

        override fun onSetFailure(p0: String?) { continuation.resumeWithException(Exception(p0)) }
    }

private fun createSdpCreationObserver(continuation: Continuation<SessionDescription>) =
    object: SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription) { continuation.resume(p0) }

        override fun onSetSuccess() { }

        override fun onCreateFailure(p0: String?) { continuation.resumeWithException(Exception(p0)) }

        override fun onSetFailure(p0: String?) { }
    }

private suspend fun PeerConnection.setLocalDescription(sdp: SessionDescription) = suspendCoroutine {
    setLocalDescription(createSdpSetObserver(it), sdp)
}

private suspend fun PeerConnection.setRemoteDescription(sdp: SessionDescription) = suspendCoroutine {
    setRemoteDescription(createSdpSetObserver(it), sdp)
}

private suspend fun PeerConnection.createAnswer() = suspendCoroutine {
    createAnswer(createSdpCreationObserver(it), MediaConstraints())
}

private fun RemoteFrame.RtcOffer.toSessionDescription(): SessionDescription =
    SessionDescription(SessionDescription.Type.OFFER, sdp)

private fun RemoteFrame.RtcCandidate.toIceCandidate(): IceCandidate =
    IceCandidate(sdpMid, sdpMLineIndex ?: 0, sdp)

private fun IceCandidate.toRemoteFrame(): RemoteFrame.RtcCandidate =
    RemoteFrame.RtcCandidate(sdp, sdpMLineIndex, sdpMid)
