package kiwi.hoonkun.ckremote.core.feeds

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kiwi.hoonkun.ckremote.core.feeds.contents.YoutubeFrame
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIScrollViewContentInsetAdjustmentBehavior
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration


@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun FeedsYoutubeFrame(
    node: YoutubeFrame,
    modifier: Modifier
) {
    UIKitView(
        factory = {
            WKWebView(
                frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
                configuration = WKWebViewConfiguration().apply {
                    allowsAirPlayForMediaPlayback = true
                    allowsInlineMediaPlayback = true
                }
            )
        },
        update = {
            it.scrollView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentBehavior.UIScrollViewContentInsetAdjustmentNever
            it.loadHTMLString(buildYoutubeFrameSource(node.id), null)
        },
        modifier = modifier
    )
}