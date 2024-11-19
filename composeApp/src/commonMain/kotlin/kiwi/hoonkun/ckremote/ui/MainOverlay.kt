package kiwi.hoonkun.ckremote.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import ckremote.composeapp.generated.resources.Res
import ckremote.composeapp.generated.resources.disconnect
import ckremote.composeapp.generated.resources.reset_controllers
import ckremote.composeapp.generated.resources.resume
import ckremote.composeapp.generated.resources.terminal
import kiwi.hoonkun.ckremote.core.feeds.Feed
import kiwi.hoonkun.ckremote.core.player.CommunicatorState
import kiwi.hoonkun.ckremote.core.player.RemoteServer
import kiwi.hoonkun.ckremote.core.player.rememberRemoteServers
import kiwi.hoonkun.ckremote.ui.overlay.ActionsMenu
import kiwi.hoonkun.ckremote.ui.overlay.ActionsMenuItem
import kiwi.hoonkun.ckremote.ui.overlay.CopyrightsText
import kiwi.hoonkun.ckremote.ui.overlay.DummyFeedDetail
import kiwi.hoonkun.ckremote.ui.overlay.DummyServerMutationUi
import kiwi.hoonkun.ckremote.ui.overlay.FeedDetail
import kiwi.hoonkun.ckremote.ui.overlay.FeedsColumn
import kiwi.hoonkun.ckremote.ui.overlay.RandomBackground
import kiwi.hoonkun.ckremote.ui.overlay.ServerMutationUi
import kiwi.hoonkun.ckremote.ui.overlay.ServersColumn
import kiwi.hoonkun.ckremote.ui.overlay.TitleBar
import kiwi.hoonkun.ckremote.utils.compose.BlurEffect
import kiwi.hoonkun.ckremote.utils.compose.NativeBackPressEffect
import kiwi.hoonkun.ckremote.utils.compose.horizontalSafeAreaPadding


@Composable
fun AnimatedVisibilityScope.MainOverlay(
    communicatorState: CommunicatorState?,
    activeServer: RemoteServer?,
    setActiveServer: (RemoteServer?) -> Unit,
    requestClose: () -> Unit
) {

    val (servers, mutate) = rememberRemoteServers()

    var mutationTarget by remember { mutableStateOf<RemoteServer?>(null) }
    var feedDetailTarget by remember { mutableStateOf<Feed?>(null) }

    val animatedBlur by animateFloatAsState(if (mutationTarget != null || feedDetailTarget != null) 60f else 0f)


    NativeBackPressEffect(activeServer != null) { requestClose() }

    RandomBackground(visible = communicatorState == null)

    MainOverlayRootBox(
        modifier = Modifier
            .graphicsLayer { renderEffect = BlurEffect(radius = animatedBlur) },
    ) {

        FeedsColumn(
            narrowHeight = activeServer != null,
            requestDetail = { feedDetailTarget = it },
            modifier = Modifier.align(Alignment.TopEnd),
        )

        ServersMenu {

            TitleBar()

            ServersColumn(
                items = servers,
                activeServer = activeServer,
                onItemClick = setActiveServer,
                onItemLongClick = { mutationTarget = it }
            )

            CopyrightsText()

        }

        ActionsMenu(
            hasActiveServer = activeServer != null,
            trueContent = {
                ActionsMenuItem(
                    text = "게임으로 돌아가기",
                    icon = Res.drawable.resume,
                    iconScale = 1.2f,
                    onClick = requestClose
                )
                ActionsMenuItem(
                    text = "컨트롤러 입력 초기화",
                    icon = Res.drawable.reset_controllers,
                    onClick = { communicatorState?.emulateKey("RST") }
                )
                ActionsMenuItem(
                    text = "연결 해제하기",
                    icon = Res.drawable.disconnect,
                    onClick = { setActiveServer(null) }
                )
            },
            falseContent = {
                ActionsMenuItem(
                    text = if (servers.size > 0) "다른 서버에 연결" else "새 서버 설정 추가",
                    icon = Res.drawable.terminal,
                    onClick = { mutationTarget = RemoteServer.Empty() }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .animateEnterExit(
                    enter = slideInHorizontally { it / 6 },
                    exit = slideOutHorizontally { it / 6 },
                ),
        )

    }

    FeedDetailAnimator(
        targetState = feedDetailTarget,
        nullContent = { DummyFeedDetail() },
        modifier = Modifier.fillMaxSize()
    ) { target ->
        FeedDetail(
            target = target,
            requestClose = { feedDetailTarget = null },
        )
    }

    ServerMutationUiAnimator(
        targetState = mutationTarget,
        nullContent = { DummyServerMutationUi() }
    ) { target ->
        ServerMutationUi(
            initialStates = target,
            identFactory = { (servers.maxOfOrNull { it.ident } ?: -1) + 1 },
            requestSave = save@ { created, connect ->
                mutate { servers.add(created) }

                if (!connect) return@save
                setActiveServer(created)
            },
            requestEdit = edit@ { edited ->
                val found = servers.find { edited.ident == it.ident } ?: return@edit { }
                val index = servers.indexOf(found)

                mutate { servers[index] = edited }
                return@edit { mutate { servers[index] = found } }
            },
            requestDelete = delete@ { ident ->
                val found = servers.find { it.ident == ident } ?: return@delete { }
                val index = servers.indexOf(found)

                mutate { servers.remove(found) }
                return@delete { mutate { servers.add(index, found) } }
            },
            requestClose = { mutationTarget = null }
        )
    }
}

@Composable
fun MainOverlayRootBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) =
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.horizontalGradient(
                    0.0f to Color.Black,
                    0.3f to Color.Black.copy(alpha = 0f),
                    0.5f to Color.Black.copy(alpha = 0f),
                    1.0f to Color.Black
                )
            )
            .then(modifier)
            .horizontalSafeAreaPadding(),
        content = content
    )

@Composable
fun AnimatedVisibilityScope.ServersMenu(
    content: @Composable ColumnScope.() -> Unit
) =
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .animateEnterExit(
                enter = slideInHorizontally { -it / 8 },
                exit = slideOutHorizontally { -it / 8 },
            ),
        content = content
    )

@Composable
fun ServerMutationUiAnimator(
    targetState: RemoteServer?,
    nullContent: @Composable () -> Unit,
    content: @Composable AnimatedContentScope.(RemoteServer) -> Unit
) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) {
        if (it == null) nullContent()
        else content(it)
    }


@Composable
fun FeedDetailAnimator(
    targetState: Feed?,
    nullContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedContentScope.(Feed) -> Unit
) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = modifier
    ) {
        if (it == null) nullContent()
        else content(it)
    }
