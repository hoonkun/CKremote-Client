package kiwi.hoonkun.ckremote.utils.compose

import androidx.compose.runtime.Composable


@Composable
actual fun NativeBackPressEffect(enabled: Boolean, block: () -> Unit) {
    // Do nothing, There is no back handling in iOS
}