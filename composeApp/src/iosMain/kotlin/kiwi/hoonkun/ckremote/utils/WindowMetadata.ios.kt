package kiwi.hoonkun.ckremote.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIScreen


@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberWindowMetadata(): WindowMetadata = remember {
    UIScreen.mainScreen.bounds.useContents {
        WindowMetadata(
            width = size.width,
            height = size.height
        )
    }
}
