package kiwi.hoonkun.ckremote.utils.compose

import androidx.compose.runtime.Composable


@Composable
expect fun AppStateBackgroundEffect(
    key1: Any? = null,
    key2: Any? = null,
    key3: Any? = null,
    onBackground: () -> Unit
)

@Composable
expect fun AppStateForegroundEffect(
    key1: Any? = null,
    key2: Any? = null,
    key3: Any? = null,
    onForeground: () -> Unit
)
