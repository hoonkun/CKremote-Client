package kiwi.hoonkun.ckremote

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext


@Composable
actual fun LaunchArgumentsProvider(initialLaunchArgs: LaunchArguments, content: @Composable () -> Unit) {
    val activity = LocalContext.current as? MainActivity

    if (activity == null) {
        CompositionLocalProvider(
            LocalLaunchArgs provides initialLaunchArgs,
            content = content
        )
    } else {
        val launchArgs by activity.collectLaunchArgumentsAsState(initialLaunchArgs)

        CompositionLocalProvider(
            LocalLaunchArgs provides launchArgs,
            content = content
        )
    }
}