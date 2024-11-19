package kiwi.hoonkun.ckremote.core.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kiwi.hoonkun.ckremote.ui.standalone.SnackBar
import kiwi.hoonkun.ckremote.utils.compose.AppStateBackgroundEffect
import kiwi.hoonkun.ckremote.utils.compose.AppStateForegroundEffect
import kiwi.hoonkun.ckremote.utils.compose.rememberMutableRefOf
import kiwi.hoonkun.ckremote.utils.rememberWindowMetadata
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


@Stable
class CommunicatorState(
    currentServer: RemoteServer
) {

    private val communicator: Communicator = Communicator(currentServer)

    val stateFlow: StateFlow<Communicator.State> get() = communicator.stateFlow
    val errorFlow: StateFlow<Communicator.Error?> get() = communicator.errorFlow

    val remoteWindowRatio: Double get() = communicator.remoteRatio

    val remoteStream = Communicator.RemoteStream(communicator)

    fun start(request: RemoteFrame.InitRequest) {
        communicator.launchConnection(request)
    }

    fun stop() {
        communicator.cancelConnection()
        communicator.closeRtcElements()
    }

    fun emulateKey(command: String) {
        communicator.writeChannel(command)
    }

}

@Composable
fun CommunicatorState.collectConnectingAsState() =
    produceState(!stateFlow.value.completed) { stateFlow.collect { value = !it.completed } }

@Composable
fun rememberPreservedCommunicatorState(
    currentServer: RemoteServer?,
    setCurrentServer: (RemoteServer?) -> Unit
): State<CommunicatorState?> {

    val scope = rememberCoroutineScope()

    val state = remember { mutableStateOf<CommunicatorState?>(null) }
    val disconnectedByAppState = rememberMutableRefOf { false }

    val windowMetadata = rememberWindowMetadata()

    DisposableEffect(currentServer) {
        currentServer ?: return@DisposableEffect NoOp

        val newState = CommunicatorState(currentServer).also {
            it.start(RemoteFrame.InitRequest(currentServer.sentence, windowMetadata.ratio))
        }
        val connectionSnackIdent = SnackBar.make(autoHideDuration = -1) { ident ->
            ConnectionStateSnackContent(newState, ident)
        }

        state.value = newState

        onDispose {
            newState.stop()
            SnackBar.destroy(connectionSnackIdent)

            val disconnectMessage =
                if (disconnectedByAppState.value) "앱이 백그라운드로 이동하여 연결을 해제했습니다."
                else "연결을 해제했습니다."

            SnackBar.make { Text(text = disconnectMessage) }
        }
    }

    AppStateBackgroundEffect(currentServer) {
        if (currentServer == null) return@AppStateBackgroundEffect

        disconnectedByAppState.value = true
        setCurrentServer(null)
    }

    AppStateForegroundEffect {
        if (!disconnectedByAppState.value) return@AppStateForegroundEffect

        scope.launch {
            try { delay(100) }
            finally { disconnectedByAppState.value = false }
        }
    }

    return state

}

@Composable
fun ConnectionStateSnackContent(state: CommunicatorState, ident: String) {
    val currentState by state.stateFlow.collectAsState()

    LaunchedEffect(currentState) {
        if (!currentState.completed) return@LaunchedEffect

        delay(3000)
        SnackBar.destroy(ident)
    }

    AnimatedContent(
        targetState = currentState,
        transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) }
    ) {
        Text(text = it.description)
    }

}

private val DisposableEffectScope.NoOp get() = onDispose { }
