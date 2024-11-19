package kiwi.hoonkun.ckremote.core.player

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kiwi.hoonkun.ckremote.core.networking.DefaultHttpClientEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.io.IOException


expect class Communicator(
    currentServer: RemoteServer
): SignalingCommunicator, RtcCommunicator {
    var remoteRatio: Double

    interface State {
        val description: String
        val completed: Boolean

        data object Unspecified: State
    }

    interface Error {
        val description: String
    }

    class RemoteStream(communicator: Communicator)
}

fun buildCommunicatorClient() = HttpClient(DefaultHttpClientEngine) {
    install(WebSockets) {
        pingIntervalMillis = 10000
    }
}

interface SignalingCommunicator {

    val server: RemoteServer

    val coroutineScope: CoroutineScope

    val client: HttpClient

    var connectionJob: Job?
    var socketSession: ClientWebSocketSession?

    val stateFlow: MutableStateFlow<Communicator.State>
    val errorFlow: MutableStateFlow<Communicator.Error?>

    fun launchConnection(
        request: RemoteFrame.InitRequest
    ) = coroutineScope.launch {
        stateFlow.value = State.Connecting

        try {
            client.webSocket(
                method = HttpMethod.Get,
                host = server.host,
                port = server.port,
                path = "/player",
                block = { handleSession(request) }
            )
            socketSession = null
        } catch (exception: IOException) {
            val message = exception.message ?: "Unknown Error"

            errorFlow.value =
                if (message.lowercase().contains("hostname could not be found")) {
                    Error.UnknownHost
                } else if (message.lowercase().contains("unreachable")) {
                    Error.Unreachable
                } else {
                    Error.Unknown
                }
        }
    }.also { connectionJob = it }

    fun cancelConnection() {
        connectionJob?.cancel()
        socketSession = null
    }

    suspend fun DefaultClientWebSocketSession.handleSession(
        request: RemoteFrame.InitRequest
    ) {
        socketSession = this

        writeSocket(request)
        stateFlow.value = State.PendingInitializeResponse
        try {
            while (true) {
                val received = incoming.receive() as? Frame.Text ?: continue
                val text = received.readText().replace(Regex("\"type\":\"(.+?)\","), "")

                val frame =
                    try { text.decodeToRemoteFrame() }
                    catch (e: Exception) {
                        e.printStackTrace()
                        continue
                    }

                when (frame) {
                    is RemoteFrame.InitResponse -> handleInit(frame)
                    is RemoteFrame.RtcOffer -> handleOffer(frame)
                    is RemoteFrame.RtcCandidate -> handleCandidate(frame)
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            println("E SignalingCommunicator::ClosedReceiveChannelException - ${closeReason.await()}")
        } catch (e: ClosedSendChannelException) {
            println("E SignalingCommunicator::ClosedSendChannelException - ${closeReason.await()}")
        } catch (e: CancellationException) {
            println("E SignalingCommunicator::CancellationException")
        }
    }

    suspend fun writeSocket(frame: RemoteFrame.Sending) {
        socketSession?.send(Frame.Text(frame.encodeToString()))
    }

    fun handleInit(frame: RemoteFrame.InitResponse) {
        stateFlow.value = State.PendingOffer
    }

    suspend fun handleOffer(frame: RemoteFrame.RtcOffer) {
        stateFlow.value = State.GatheringIceStates
    }

    suspend fun handleCandidate(frame: RemoteFrame.RtcCandidate)

    enum class State(
        override val description: String,
        override val completed: Boolean = false
    ): Communicator.State {
        Connecting(description = "소켓 연결 중"),
        PendingInitializeResponse(description = "초기화 응답 대기 중"),
        PendingOffer(description = "Offer 대기 중"),
        GatheringIceStates(description = "Ice 후보군 수집 중")
    }

    enum class Error(
        override val description: String
    ): Communicator.Error {
        UnknownHost(description = "호스트 주소를 찾을 수 없었어요. 설정값에 오타가 없는지 확인해주세요!"),
        Unreachable(description = "서버에 접속할 수 없었어요. 인터넷 연결 상태를 확인해주세요!"),
        Unknown(description = "알 수 없는 오류가 발생했어요.")
    }

}

interface RtcCommunicator {

    val stateFlow: MutableStateFlow<Communicator.State>
    val errorFlow: MutableStateFlow<Communicator.Error?>

    suspend fun dispatchServerOffer(frame: RemoteFrame.RtcOffer)

    suspend fun dispatchServerCandidate(frame: RemoteFrame.RtcCandidate)

    fun writeChannel(payload: String)

    fun closeRtcElements()

    enum class State(
        override val description: String,
        override val completed: Boolean = false
    ): Communicator.State {
        IceGathering(description = "Ice 후보군 수집 중"),
        IceGathered(description = "Ice 후보군 수집 완료"),
        IceStateChecking(description = "Ice 연결 확인 중"),
        IceStateConnected(description = "연결되었습니다.", completed = true),
    }

    enum class Error(
        override val description: String
    ): Communicator.Error {
        // TODO
    }

}