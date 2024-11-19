package kiwi.hoonkun.ckremote

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import kiwi.hoonkun.ckremote.core.player.RemoteServer


data class LaunchArguments(
    val initialRemoteServer: RemoteServer? = null,
) {
    companion object
}

val LocalLaunchArgs = compositionLocalOf { LaunchArguments() }

@Composable
expect fun LaunchArgumentsProvider(initialLaunchArgs: LaunchArguments, content: @Composable () -> Unit)
