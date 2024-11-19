package kiwi.hoonkun.ckremote.core.feeds

import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kiwi.hoonkun.ckremote.core.feeds.contents.YoutubeFrame


@Composable
actual fun FeedsYoutubeFrame(
    node: YoutubeFrame,
    modifier: Modifier
) {
    AndroidView(
        factory = {
            WebView(it).apply {
                settings.javaScriptEnabled = true

                setBackgroundColor(Color.TRANSPARENT)

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = {
            it.loadData(buildYoutubeFrameSource(node.id), "text/html", "UTF-8")
        },
        modifier = modifier
    )
}