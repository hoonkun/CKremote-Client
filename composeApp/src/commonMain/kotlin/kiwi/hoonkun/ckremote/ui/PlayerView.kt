package kiwi.hoonkun.ckremote.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ckremote.composeapp.generated.resources.Res
import ckremote.composeapp.generated.resources.controllers_off
import ckremote.composeapp.generated.resources.controllers_on
import ckremote.composeapp.generated.resources.icon_menu
import kiwi.hoonkun.ckremote.core.player.CommunicatorState
import kiwi.hoonkun.ckremote.core.player.RemoteStream
import kiwi.hoonkun.ckremote.core.player.collectConnectingAsState
import kiwi.hoonkun.ckremote.ui.controller.Action
import kiwi.hoonkun.ckremote.ui.controller.Button
import kiwi.hoonkun.ckremote.ui.controller.DPadStickUnderlay
import kiwi.hoonkun.ckremote.ui.controller.Grip
import kiwi.hoonkun.ckremote.ui.controller.Key
import kiwi.hoonkun.ckremote.ui.controller.KeyState
import kiwi.hoonkun.ckremote.ui.controller.Keys
import kiwi.hoonkun.ckremote.ui.controller.Stick
import kiwi.hoonkun.ckremote.ui.controller.StickState
import kiwi.hoonkun.ckremote.ui.controller.Trigger
import kiwi.hoonkun.ckremote.utils.compose.BlurEffect
import kiwi.hoonkun.ckremote.utils.compose.blockBehindTouch
import kiwi.hoonkun.ckremote.utils.compose.horizontalSafeAreaPadding
import kiwi.hoonkun.ckremote.utils.compose.nullIndicationClickable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource


@Composable
fun AnimatedContentScope.PlayerView(
    communicatorState: CommunicatorState,
    mainMenuShowing: Boolean = false,
    requestMainMenu: () -> Unit
) {

    val density = LocalDensity.current

    var controllerEnabled by remember { mutableStateOf(true) }

    val error by communicatorState.errorFlow.collectAsState()
    val errorBlurRadius by animateFloatAsState(if (error != null) with(density) { 30.dp.toPx() } else 0f)

    val connecting by communicatorState.collectConnectingAsState()

    RemoteStream(
        state = communicatorState,
        underlayed = mainMenuShowing,
        modifier = Modifier
            .fillMaxSize()
    )

    Box (
        modifier = Modifier
            .horizontalSafeAreaPadding()
            .animateEnterExit(
                enter = slideInVertically { with(density) { 10.dp.roundToPx() } },
                exit = slideOutVertically { with(density) { 10.dp.roundToPx() } }
            )
            .fillMaxSize()
    ) {
        Controllers(
            visible = controllerEnabled,
            onKey = { key, state ->
                communicatorState.emulateKey("${key.type} ${key.value} ${state.value}")
            },
            onStick = stickHandler@ { key, state, edgeInPrevious, edgeInCurrent, edgeActiveTrigger ->
                communicatorState.emulateKey("STK ${key.value} ${state.angle} ${state.distance}")

                edgeActiveTrigger ?: return@stickHandler

                if (edgeInPrevious && !edgeInCurrent) {
                    communicatorState.emulateKey("TRG ${edgeActiveTrigger.value} UP")
                } else if (!edgeInPrevious && edgeInCurrent) {
                    communicatorState.emulateKey("TRG ${edgeActiveTrigger.value} DOWN")
                }
            },
            modifier = Modifier
                .graphicsLayer {
                    renderEffect = BlurEffect(radius = errorBlurRadius)
                }
        )

        FunctionButton(
            icon =
                if (controllerEnabled) Res.drawable.controllers_off
                else Res.drawable.controllers_on,
            onClick = { controllerEnabled = !controllerEnabled },
            modifier = Modifier.align(Alignment.TopStart)
        )

        AnimatedContent(
            targetState = error,
            transitionSpec = {
                val enter = fadeIn() + scaleIn(initialScale = 1.1f)
                val exit = fadeOut() + scaleOut(targetScale = 1.1f)
                val sizeTransform = SizeTransform(clip = false)

                enter togetherWith exit using sizeTransform
            }
        ) {
            if (it != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .blockBehindTouch()
                        .background(Color.Black.copy(alpha = 0.7f))
                ) {
                    Text(
                        text = "앗, 문제가 발생했어요!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = it.description,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize())
            }
        }

        FunctionButton(
            icon = Res.drawable.icon_menu,
            onClick = requestMainMenu,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }


    AnimatedVisibility(
        visible = connecting,
        enter = fadeIn() + scaleIn(initialScale = 1.2f),
        exit = fadeOut() + scaleOut(targetScale = 1.2f)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
        ) {
            Text(
                text = "연결하는 중입니다",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }

}

@Composable
fun DummyPlayerView() = Spacer(modifier = Modifier.fillMaxSize())

private val TopActionModifier = Modifier.size(75.dp, 50.dp).offset(y = (-8).dp)

@Composable
private fun FunctionButton(
    icon: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(icon),
        contentDescription = null,
        tint = Color.White.copy(alpha = Keys.ControllerInactiveAlpha),
        modifier = modifier
            .nullIndicationClickable { onClick() }
            .padding(15.dp)
            .size(30.dp)
            .offset(y = 6.dp)
    )
}

@Composable
private fun BoxScope.Controllers(
    visible: Boolean,
    onKey: (Key, KeyState<*>) -> Unit,
    onStick: (Stick, StickState, Boolean, Boolean, Trigger?) -> Unit,
    modifier: Modifier = Modifier
) {

    var edgeActivateTrigger by remember { mutableStateOf(Trigger.Right) }

    val onAnalog = { key: Key, value: Int ->
        onKey(key, if (value == 0) KeyState.Up else KeyState.Down)
    }
    val onDown = { key: Key ->
        onKey(key, KeyState.Down)
    }
    val onUp = { key: Key ->
        onKey(key, KeyState.Up)
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { -it / 3 },
        exit = fadeOut() + slideOutVertically { -it / 3 },
        modifier = modifier
            .align(Alignment.TopEnd)
    ) {
        Row {
            Action(type = Action.Menu, onDown = onDown, onUp = onUp, modifier = TopActionModifier)
            Action(type = Action.Select, onDown = onDown, onUp = onUp, modifier = TopActionModifier)
            Spacer(modifier = Modifier.size(60.dp))
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally { -it / 6 },
        exit = fadeOut() + slideOutHorizontally { -it / 6 },
        modifier = modifier
            .align(Alignment.BottomStart)
    ) {
        Box(
            contentAlignment = Alignment.TopStart
        ) {
            Stick(
                type = Stick.Left,
                onPull = { stick, state, edgeInPrevious, edgeInCurrent ->
                    onStick(stick, state, edgeInPrevious, edgeInCurrent, null)
                },
                overlay = { DPadStickUnderlay(it) },
                modifier = Modifier
                    .padding(top = 40.dp, start = 75.dp)
                    .size(175.dp)
            )
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Trigger(type = Trigger.Left, onAnalog = onAnalog, stickActivate = edgeActivateTrigger == Trigger.Left)
                Button(type = Button.Left, onDown = onDown, onUp = onUp)
                Grip(type = Grip.Left1, onDown = onDown, onUp = onUp)
                Grip(type = Grip.Left2, onDown = onDown, onUp = onUp)

                Row(
                    modifier = Modifier
                        .padding(start = Keys.ActionRowPadding.dp)
                ) {
                    Action(type = Action.X, onDown = onDown, onUp = onUp)
                    Action(type = Action.Y, onDown = onDown, onUp = onUp)
                }
            }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally { it / 6 },
        exit = fadeOut() + slideOutHorizontally { it / 6 },
        modifier = modifier
            .align(Alignment.BottomEnd)
    ) {
        Box(
            contentAlignment = Alignment.TopEnd
        ) {
            Stick(
                type = Stick.Right,
                onPull = { stick, state, edgeInPrevious, edgeInCurrent ->
                    onStick(stick, state, edgeInPrevious, edgeInCurrent, edgeActivateTrigger)
                },
                onClick = {
                    edgeActivateTrigger =
                        if (edgeActivateTrigger == Trigger.Right) Trigger.Left
                        else Trigger.Right
                },
                edgeDistance = 10.dp,
                modifier = Modifier
                    .padding(top = 40.dp, end = 75.dp)
                    .size(175.dp)
            )
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Trigger(type = Trigger.Right, onAnalog = onAnalog, stickActivate = edgeActivateTrigger == Trigger.Right)
                Button(type = Button.Right, onDown = onDown, onUp = onUp)
                Grip(type = Grip.Right1, onDown = onDown, onUp = onUp)
                Grip(type = Grip.Right2, onDown = onDown, onUp = onUp)

                Row(
                    modifier = Modifier
                        .padding(end = Keys.ActionRowPadding.dp)
                ) {
                    Action(type = Action.A, onDown = onDown, onUp = onUp)
                    Action(type = Action.B, onDown = onDown, onUp = onUp)
                }
            }
        }
    }
}
