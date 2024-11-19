package kiwi.hoonkun.ckremote.utils.compose

import androidx.compose.runtime.Composable


@Composable
expect fun NativeBackPressEffect(enabled: Boolean = true, block: () -> Unit)