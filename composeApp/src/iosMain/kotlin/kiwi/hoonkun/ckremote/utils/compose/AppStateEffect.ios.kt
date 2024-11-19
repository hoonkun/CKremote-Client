package kiwi.hoonkun.ckremote.utils.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotificationName
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification


@Composable
actual fun AppStateBackgroundEffect(
    key1: Any?,
    key2: Any?,
    key3: Any?,
    onBackground: () -> Unit
) {
    NotificationEffect(
        key1 = key1,
        key2 = key2,
        key3 = key3,
        name = UIApplicationDidEnterBackgroundNotification,
        block = onBackground
    )
}

@Composable
actual fun AppStateForegroundEffect(
    key1: Any?,
    key2: Any?,
    key3: Any?,
    onForeground: () -> Unit
) {
    NotificationEffect(
        key1 = key1,
        key2 = key2,
        key3 = key3,
        name = UIApplicationWillEnterForegroundNotification,
        block = onForeground
    )
}

@Composable
fun NotificationEffect(
    key1: Any?,
    key2: Any?,
    key3: Any?,
    name: NSNotificationName,
    block: () -> Unit
) {
    DisposableEffect(key1, key2, key3) {
        val observer = NSNotificationCenter.defaultCenter
            .addObserverForName(
                name = name,
                `object` = null,
                queue = null,
                usingBlock = { block() }
            )

        onDispose {
            NSNotificationCenter.defaultCenter
                .removeObserver(
                    observer = observer,
                    name = name,
                    `object` = null
                )
        }
    }
}