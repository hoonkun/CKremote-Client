package kiwi.hoonkun.ckremote

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kiwi.hoonkun.ckremote.core.AppShortcutApplyEffect
import kiwi.hoonkun.ckremote.core.player.rememberPreservedCommunicatorState
import kiwi.hoonkun.ckremote.ui.DummyPlayerView
import kiwi.hoonkun.ckremote.ui.MainOverlay
import kiwi.hoonkun.ckremote.ui.NativeLayerBackground
import kiwi.hoonkun.ckremote.ui.PlayerView
import kiwi.hoonkun.ckremote.ui.standalone.SnackBars
import kiwi.hoonkun.ckremote.utils.compose.BlurEffect
import kiwi.hoonkun.ckremote.utils.compose.NativeBackPressEffect
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(initialLaunchArgs: LaunchArguments) {
    AppProviders(initialLaunchArgs) {
        AppRootBox {
            NativeLayerBackground()
            AppContent()
            SnackBars()
        }
    }
}

@Composable
fun AppContent() {

    val (initialServer) = LocalLaunchArgs.current

    var activeServer by remember(initialServer) { mutableStateOf(initialServer) }
    val communicatorState by rememberPreservedCommunicatorState(
        currentServer = activeServer,
        setCurrentServer = { activeServer = it }
    )

    var isOverlayActive by remember { mutableStateOf(false) }
    val playerBlurRadius by animateDpAsState(
        targetValue = if (activeServer == null || isOverlayActive) 50.dp else 0.dp,
        animationSpec = tween(durationMillis = 500)
    )


    NativeBackPressEffect(activeServer != null) { isOverlayActive = true }

    AppShortcutApplyEffect()

    AnimatedContent(
        targetState = communicatorState,
        transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) },
        modifier = Modifier.graphicsLayer { renderEffect = BlurEffect(playerBlurRadius.toPx()) }
    ) { capturedCommunicatorState ->
        if (capturedCommunicatorState != null) {
            PlayerView(
                communicatorState = capturedCommunicatorState,
                mainMenuShowing = activeServer == null || isOverlayActive,
                requestMainMenu = { isOverlayActive = true }
            )
        } else {
            DummyPlayerView()
        }
    }

    AnimatedVisibility(
        visible = activeServer == null || isOverlayActive,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        MainOverlay(
            communicatorState = communicatorState,
            activeServer = activeServer,
            setActiveServer = {
                activeServer = if (activeServer == it) null else it
                if (activeServer != null) isOverlayActive = false
            },
            requestClose = { isOverlayActive = false }
        )
    }

}

@Composable
fun AppProviders(
    initialLaunchArgs: LaunchArguments,
    content: @Composable () -> Unit
) {
    LaunchArgumentsProvider(initialLaunchArgs) {
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(color = Color.White),
            LocalContentColor provides Color.White,
            content = content
        )
    }
}

@Composable
fun AppRootBox(
    content: @Composable BoxScope.() -> Unit
) =
    Box(
        modifier = Modifier.fillMaxSize(),
        content = content
    )