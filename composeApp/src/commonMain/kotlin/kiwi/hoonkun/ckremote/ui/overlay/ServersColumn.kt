package kiwi.hoonkun.ckremote.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ckremote.composeapp.generated.resources.Res
import ckremote.composeapp.generated.resources.current_server
import kiwi.hoonkun.ckremote.core.player.RemoteServer
import kiwi.hoonkun.ckremote.utils.compose.nullIndicationCombinedClickable
import org.jetbrains.compose.resources.painterResource


@Composable
fun ColumnScope.ServersColumn(
    items: List<RemoteServer>,
    activeServer: RemoteServer?,
    onItemClick: (RemoteServer) -> Unit,
    onItemLongClick: (RemoteServer) -> Unit
) {

    LazyColumn(
        modifier = Modifier.weight(1f)
    ) {
        item {
            Text(
                text = "저장된 서버",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
            )
            if (items.isEmpty()) {
                Text(
                    text = "저장된 서버가 없습니다.\n오른쪽 아래의 '새 서버 설정 추가' 메뉴를 통해 서버를 추가해주세요.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
            }
        }
        items(items, key = { it.ident }) {
            ServerItem(
                item = it,
                enabled = activeServer == null,
                selected = activeServer == it,
                onClick = onItemClick,
                onLongClick = onItemLongClick,
                modifier = Modifier.animateItem()
            )
        }
    }
}


@Composable
private fun ServerItem(
    item: RemoteServer,
    enabled: Boolean,
    selected: Boolean,
    onClick: (RemoteServer) -> Unit,
    onLongClick: (RemoteServer) -> Unit,
    modifier: Modifier = Modifier
) {

    val animatedAlpha by animateFloatAsState(targetValue = if (enabled || selected) 1f else 0.3f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .nullIndicationCombinedClickable(
                enabled = enabled,
                onLongClick = { onLongClick(item) },
                onClick = { onClick(item) }
            )
            .graphicsLayer { alpha = animatedAlpha }
            .padding(vertical = 8.dp)
    ) {
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn() + slideInHorizontally() + expandHorizontally(clip = false),
            exit = fadeOut() + slideOutHorizontally() + shrinkHorizontally(clip = false)
        ) {
            Icon(
                painter = painterResource(Res.drawable.current_server),
                contentDescription = null,
                modifier = Modifier.padding(end = 12.dp).size(32.dp)
            )
        }
        Column {
            Text(
                text = item.name,
                fontSize = 16.sp
            )
            Text(
                text = buildAnnotatedString {
                    append("${item.host}:${item.port}")

                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.3f))) {
                        append(" \u2014 ")
                    }

                    append("\u2022".repeat(item.sentence.length)) // FIXME: Ellipsis
                },
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}
