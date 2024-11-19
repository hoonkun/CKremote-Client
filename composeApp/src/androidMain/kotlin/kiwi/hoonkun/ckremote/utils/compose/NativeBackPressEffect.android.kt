package kiwi.hoonkun.ckremote.utils.compose

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable


@Composable
actual fun NativeBackPressEffect(enabled: Boolean, block: () -> Unit) {
    BackHandler(enabled = enabled, onBack = block)
}