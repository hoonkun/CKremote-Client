package kiwi.hoonkun.ckremote.ui

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView


@Composable
actual fun NativeLayerBackground() {
    AndroidView(
        factory = { View(it).apply { setBackgroundColor(android.graphics.Color.BLACK) } },
        modifier = Modifier.fillMaxSize()
    )
}