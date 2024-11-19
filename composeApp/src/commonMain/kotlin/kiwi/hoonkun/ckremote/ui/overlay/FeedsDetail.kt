package kiwi.hoonkun.ckremote.ui.overlay

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ckremote.composeapp.generated.resources.Res
import ckremote.composeapp.generated.resources.icon_back
import kiwi.hoonkun.ckremote.core.feeds.Feed
import kiwi.hoonkun.ckremote.core.feeds.FeedContent
import kiwi.hoonkun.ckremote.core.feeds.Feeds
import kiwi.hoonkun.ckremote.core.feeds.contents.rememberFeedContentParagraphs
import kiwi.hoonkun.ckremote.utils.compose.NativeBackPressEffect
import kiwi.hoonkun.ckremote.utils.compose.blockBehindTouch
import kiwi.hoonkun.ckremote.utils.compose.horizontalSafeAreaPadding
import kiwi.hoonkun.ckremote.utils.rememberFormattedDate
import kiwi.hoonkun.ckremote.utils.rememberUserSpaceStore
import org.jetbrains.compose.resources.painterResource


@Composable
fun AnimatedContentScope.FeedDetail(
    target: Feed,
    requestClose: () -> Unit,
) {

    val density = LocalDensity.current
    val articleSlideOffset = with(density) { 30.dp.roundToPx() }

    val store = rememberUserSpaceStore()
    val preview by produceState(Feeds.imageBitmaps[target.gid]) {
        if (value != null) {
            value = Feeds.fetchImage(target.gid, target.url).also { Feeds.cacheImages(store) }
        }
    }

    val formattedDate = rememberFormattedDate(target.date)
    val paragraphs = rememberFeedContentParagraphs(target)

    val articleScrollState = rememberScrollState()


    NativeBackPressEffect { requestClose() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .horizontalSafeAreaPadding()
            .blockBehindTouch()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(articleScrollState)
                .animateEnterExit(
                    enter = slideInVertically { articleSlideOffset },
                    exit = slideOutVertically { articleSlideOffset }
                )
        ) {
            val capturedPreview = preview
            if (capturedPreview != null) {
                Image(
                    bitmap = capturedPreview,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .requiredWidthIn(max = 500.dp)
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = -16.dp.roundToPx() + articleScrollState.value / 2) }
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp)
                )
            }

            Column(
                modifier = Modifier
                    .offset(y = (-32).dp)
                    .requiredWidthIn(max = 500.dp)
                    .background(Color(0xff161616))
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = target.title,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .padding(horizontal = 16.dp)
                )
                Text(
                    text = "$formattedDate \u2014 ${target.author}",
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .padding(horizontal = 16.dp)
                )
                FeedContent(
                    paragraphs = paragraphs
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp).height(60.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.icon_back),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(28.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = requestClose
                    )
            )
        }
    }

}

@Composable
fun DummyFeedDetail() =
    Box(modifier = Modifier.fillMaxSize())
