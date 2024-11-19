package kiwi.hoonkun.ckremote.ui.overlay

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kiwi.hoonkun.ckremote.core.feeds.Feed
import kiwi.hoonkun.ckremote.core.feeds.Feeds
import kiwi.hoonkun.ckremote.core.feeds.Feeds.fetchImage
import kiwi.hoonkun.ckremote.utils.compose.nullIndicationClickable
import kiwi.hoonkun.ckremote.utils.rememberFormattedDate
import kiwi.hoonkun.ckremote.utils.rememberUserSpaceStore


@Composable
fun FeedsColumn(
    narrowHeight: Boolean,
    requestDetail: (Feed) -> Unit,
    modifier: Modifier = Modifier
) {

    val store = rememberUserSpaceStore()

    val state = rememberLazyListState()

    LaunchedEffect(true) {
        with(Feeds) {
            restoreList(store)
            restoreImages(store)
        }
    }

    LaunchedEffect(true) {
        with(Feeds) {
            fetchList(3)
            cacheList(store)
        }
    }

    LazyColumn(
        state = state,
        horizontalAlignment = Alignment.End,
        contentPadding = PaddingValues(top = 32.dp),
        modifier = Modifier
            .then(modifier)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to Color(0x00000000),
                        (if (narrowHeight) 0.5f else 0.6f) to Color(0x00000000),
                        (if (narrowHeight) 0.7f else 0.8f) to Color(0xff000000),
                        1f to Color(0xff000000),
                    ),
                    blendMode = BlendMode.DstOut
                )
            }
    ) {
        items(Feeds.list, key = { it.gid }) {
            FeedItem(
                item = it,
                onClick = { requestDetail(it) }
            )
        }
        item {
            Spacer(
                modifier = Modifier
                    .height(175.dp)
            )
        }
    }

}

@Composable
fun FeedItem(
    item: Feed,
    onClick: () -> Unit,
) {

    val store = rememberUserSpaceStore()
    val preview by produceState(Feeds.imageBitmaps[item.gid]) {
        value = fetchImage(item.gid, item.url).also { Feeds.cacheImages(store) }
    }

    val date = rememberFormattedDate(item.date)

    Box(
        modifier = Modifier
            .requiredWidthIn(max = 325.dp)
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(5.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(5.dp))
            .background(Color(0xff161616))
            .nullIndicationClickable { onClick() }
    ) {

        preview?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        val modifierWithoutPreview = Modifier
            .fillMaxWidth()
            .padding(12.dp)

        val modifierWithPreview = Modifier
            .matchParentSize()
            .background(
                brush = Brush.verticalGradient(
                    0f to Color(0x20000000),
                    0.5f to Color(0x20000000),
                    1f to Color(0xff000000)
                ),
            )
            .padding(12.dp)

        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = if (preview == null) modifierWithoutPreview else modifierWithPreview
        ) {
            Text(
                text = item.title,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (!item.tags.contains("patchnotes")) "$date \u2014 ${item.contents}" else "$date \u2014 Patch Note",
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(0.5f)
            )
        }
    }

}
