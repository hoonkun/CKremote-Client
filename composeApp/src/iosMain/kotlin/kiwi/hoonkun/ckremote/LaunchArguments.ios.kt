package kiwi.hoonkun.ckremote


import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue


@Composable
actual fun LaunchArgumentsProvider(initialLaunchArgs: LaunchArguments, content: @Composable () -> Unit) {
    val launchArgs by AppDelegateKotlin.collectLaunchArgumentsAsState()

    CompositionLocalProvider(
        LocalLaunchArgs provides launchArgs,
        content = content
    )
}
