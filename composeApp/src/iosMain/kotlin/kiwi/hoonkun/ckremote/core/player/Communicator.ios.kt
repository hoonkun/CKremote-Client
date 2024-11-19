package kiwi.hoonkun.ckremote.core.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.setValue
import cocoapods.WebRTC.RTCConfiguration
import cocoapods.WebRTC.RTCDataBuffer
import cocoapods.WebRTC.RTCDataChannel
import cocoapods.WebRTC.RTCIceCandidate
import cocoapods.WebRTC.RTCIceConnectionState
import cocoapods.WebRTC.RTCIceGatheringState
import cocoapods.WebRTC.RTCIceServer
import cocoapods.WebRTC.RTCMediaConstraints
import cocoapods.WebRTC.RTCMediaStream
import cocoapods.WebRTC.RTCPeerConnection
import cocoapods.WebRTC.RTCPeerConnectionDelegateProtocol
import cocoapods.WebRTC.RTCPeerConnectionFactory
import cocoapods.WebRTC.RTCRtpMediaType
import cocoapods.WebRTC.RTCRtpTransceiverDirection
import cocoapods.WebRTC.RTCRtpTransceiverInit
import cocoapods.WebRTC.RTCSdpSemantics
import cocoapods.WebRTC.RTCSdpType
import cocoapods.WebRTC.RTCSessionDescription
import cocoapods.WebRTC.RTCSignalingState
import cocoapods.WebRTC.RTCVideoDecoderFactoryH264
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import io.ktor.utils.io.core.toByteArray
import kiwi.hoonkun.ckremote.Constants
import kiwi.hoonkun.ckremote.core.player.NSErrorHandler.NSErrorException
import kiwi.hoonkun.ckremote.utils.AudioSessionManager
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.placeTo
import kotlinx.cinterop.toCValues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.dataWithBytes
import platform.darwin.NSObject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


@OptIn(ExperimentalForeignApi::class)
actual class Communicator actual constructor(
    currentServer: RemoteServer
): SignalingCommunicator, RtcCommunicator {

    init {
        AudioSessionManager.dummy()
    }

    actual var remoteRatio by mutableDoubleStateOf(0.0)

    override val server: RemoteServer = currentServer

    override val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    override val client: HttpClient = buildCommunicatorClient()

    override var connectionJob: Job? = null
    override var socketSession: ClientWebSocketSession? = null

    override val stateFlow: MutableStateFlow<State> = MutableStateFlow(State.Unspecified)
    override val errorFlow: MutableStateFlow<Error?> = MutableStateFlow(null)

    private val peerDelegate = PeerConnectionDelegate()
    private val remotePeer: RTCPeerConnection = buildPeerConnection(peerDelegate)
    private var remoteChannel: RTCDataChannel? = null
    private val remoteStream: MutableStateFlow<RTCMediaStream?> = MutableStateFlow(null)

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
        remotePeer.addTransceiverOfType(RTCRtpMediaType.RTCRtpMediaTypeAudio, buildRecvOnlyRtcRtpTransceiverInit())
        remotePeer.addTransceiverOfType(RTCRtpMediaType.RTCRtpMediaTypeVideo, buildRecvOnlyRtcRtpTransceiverInit())

        remotePeer.setRemoteDescription(frame.toRTCSessionDescription())

        val answer = remotePeer.answerForConstraints(null, null)
            ?: throw RuntimeException("Invalid Answer")

        remotePeer.setLocalDescription(answer)
        writeSocket(RemoteFrame.RtcAnswer(answer.sdp))
    }

    override suspend fun dispatchServerCandidate(frame: RemoteFrame.RtcCandidate) {
        remotePeer.addIceCandidate(frame.toRTCIceCandidate())
    }

    override fun writeChannel(payload: String) {
        val remoteChannel = remoteChannel ?: return

        memScoped {
            val bytes = payload.toByteArray()
            val pointer = bytes.toCValues().placeTo(this)
            remoteChannel.sendData(RTCDataBuffer(NSData.dataWithBytes(pointer, bytes.size.toULong()), false))
        }
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

    inner class PeerConnectionDelegate: NSObject(), RTCPeerConnectionDelegateProtocol {

        @ObjCSignatureOverride
        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didAddStream: RTCMediaStream
        ) {
            coroutineScope.launch {
                while(!stateFlow.value.completed) {
                    yield()
                }
                remoteStream.value = didAddStream
                AudioSessionManager.forceToPlayback()
            }
        }

        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didGenerateIceCandidate: RTCIceCandidate
        ) {
            coroutineScope.launch {
                writeSocket(didGenerateIceCandidate.toRemoteFrame())
            }
        }

        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didOpenDataChannel: RTCDataChannel
        ) {
            remoteChannel = didOpenDataChannel
        }

        override fun peerConnection(peerConnection: RTCPeerConnection, didChangeIceConnectionState: RTCIceConnectionState) {
            when (didChangeIceConnectionState) {
                RTCIceConnectionState.RTCIceConnectionStateChecking ->
                    stateFlow.value = RtcCommunicator.State.IceStateChecking
                RTCIceConnectionState.RTCIceConnectionStateConnected ->
                    stateFlow.value = RtcCommunicator.State.IceStateConnected
                else -> { /* noop */ }
            }
        }

        override fun peerConnection(peerConnection: RTCPeerConnection, didChangeIceGatheringState: RTCIceGatheringState) {
            when (didChangeIceGatheringState) {
                RTCIceGatheringState.RTCIceGatheringStateGathering ->
                    stateFlow.value = RtcCommunicator.State.IceGathering
                RTCIceGatheringState.RTCIceGatheringStateComplete ->
                    stateFlow.value = RtcCommunicator.State.IceGathered
                else -> { /* noop */ }
            }
        }

        @ObjCSignatureOverride
        override fun peerConnection(peerConnection: RTCPeerConnection, didRemoveStream: RTCMediaStream) { }
        override fun peerConnection(peerConnection: RTCPeerConnection, didRemoveIceCandidates: List<*>) { }
        override fun peerConnection(peerConnection: RTCPeerConnection, didChangeSignalingState: RTCSignalingState) { }
        override fun peerConnectionShouldNegotiate(peerConnection: RTCPeerConnection) { }

    }

    actual class RemoteStream actual constructor(private val communicator: Communicator) {
        val flow get() = communicator.remoteStream
    }

}

@OptIn(ExperimentalForeignApi::class)
private val PeerConnectionFactory = RTCPeerConnectionFactory(
    encoderFactory = null,
    decoderFactory = RTCVideoDecoderFactoryH264()
)

@OptIn(ExperimentalForeignApi::class)
private fun buildRtcConfiguration() = RTCConfiguration()
    .apply {
        sdpSemantics = RTCSdpSemantics.RTCSdpSemanticsUnifiedPlan
        iceServers = listOf(
            RTCIceServer(
                uRLStrings = listOf(Constants.TurnServer.URI),
                username = Constants.TurnServer.USERNAME,
                credential = Constants.TurnServer.PASSWORD
            )
        )
    }

@OptIn(ExperimentalForeignApi::class)
private fun buildRtcMediaConstraints() =
    RTCMediaConstraints(null, null)

@OptIn(ExperimentalForeignApi::class)
private fun buildRecvOnlyRtcRtpTransceiverInit() =
    RTCRtpTransceiverInit()
        .apply { direction = RTCRtpTransceiverDirection.RTCRtpTransceiverDirectionRecvOnly }

@OptIn(ExperimentalForeignApi::class)
private fun buildPeerConnection(delegate: RTCPeerConnectionDelegateProtocol) =
    PeerConnectionFactory
        .peerConnectionWithConfiguration(
            configuration = buildRtcConfiguration(),
            constraints = buildRtcMediaConstraints(),
            delegate = delegate
        )

@OptIn(ExperimentalForeignApi::class)
private fun RemoteFrame.RtcOffer.toRTCSessionDescription() =
    RTCSessionDescription(RTCSdpType.RTCSdpTypeOffer, sdp)

@OptIn(ExperimentalForeignApi::class)
private fun RemoteFrame.RtcCandidate.toRTCIceCandidate() =
    RTCIceCandidate(sdp, sdpMLineIndex ?: 0, null)

@OptIn(ExperimentalForeignApi::class)
private fun RTCIceCandidate.toRemoteFrame() =
    RemoteFrame.RtcCandidate(sdp, sdpMLineIndex)

@OptIn(ExperimentalForeignApi::class)
private suspend fun RTCPeerConnection.setRemoteDescription(
    description: RTCSessionDescription
) = suspendCoroutine { continuation ->
    setRemoteDescription(description, NSErrorHandler(continuation)::resume)
}

@OptIn(ExperimentalForeignApi::class)
private suspend fun RTCPeerConnection.setLocalDescription(
    description: RTCSessionDescription
) = suspendCoroutine { continuation ->
    setLocalDescription(description, NSErrorHandler(continuation)::resume)
}

@OptIn(ExperimentalForeignApi::class)
private suspend fun RTCPeerConnection.answerForConstraints(
    mandatoryConstraints: Map<Any?, *>?,
    optionalConstraints: Map<Any?, *>?
) = suspendCoroutine { continuation ->
    answerForConstraints(RTCMediaConstraints(mandatoryConstraints, optionalConstraints)) { sdp, error ->
        if (error != null) {
            continuation.resumeWithException(error.toThrowable())
        } else {
            continuation.resume(sdp)
        }
    }
}

private fun NSError.toThrowable(): Throwable = NSErrorException(this)

private class NSErrorHandler(private val continuation: Continuation<Unit>) {
    fun resume(error: NSError?) {
        if (error != null) {
            continuation.resumeWithException(error.toThrowable())
        } else {
            continuation.resume(Unit)
        }
    }

    class NSErrorException(parent: NSError): Exception("${parent.domain}: ${parent.description}")
}