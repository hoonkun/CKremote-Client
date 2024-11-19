package kiwi.hoonkun.ckremote.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.UIColor
import platform.UIKit.UIView


@Composable
actual fun NativeLayerBackground() {
    UIKitView(
        factory = { UIView().apply { backgroundColor = UIColor.blackColor } },
        modifier = Modifier.fillMaxSize()
    )
}