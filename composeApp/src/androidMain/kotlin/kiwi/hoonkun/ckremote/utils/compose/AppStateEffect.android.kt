package kiwi.hoonkun.ckremote.utils.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.eventFlow


@Composable
actual fun AppStateBackgroundEffect(
    key1: Any?,
    key2: Any?,
    key3: Any?,
    onBackground: () -> Unit
) {

    val owner = LocalLifecycleOwner.current

    LaunchedEffect(owner, key1, key2, key3) {
        owner.lifecycle.eventFlow.collect {
            if (it == Lifecycle.Event.ON_PAUSE)
                onBackground()
        }
    }
}

@Composable
actual fun AppStateForegroundEffect(
    key1: Any?,
    key2: Any?,
    key3: Any?,
    onForeground: () -> Unit
) {

    val owner = LocalLifecycleOwner.current

    LaunchedEffect(owner, key1, key2, key3) {
        owner.lifecycle.eventFlow.collect {
            if (it == Lifecycle.Event.ON_RESUME)
                onForeground()
        }
    }
}