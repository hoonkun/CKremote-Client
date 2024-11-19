package kiwi.hoonkun.ckremote

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ComposeUIViewController
import kiwi.hoonkun.ckremote.core.AppShortcuts
import kiwi.hoonkun.ckremote.core.player.RemoteServer
import kiwi.hoonkun.ckremote.core.player.RemoteServerMutation
import kiwi.hoonkun.ckremote.utils.collectAsState
import kiwi.hoonkun.ckremote.utils.createUserSpaceStore
import kotlinx.coroutines.flow.MutableStateFlow
import platform.UIKit.UIApplicationLaunchOptionsKey
import platform.UIKit.UIApplicationLaunchOptionsShortcutItemKey
import platform.UIKit.UIApplicationShortcutItem
import platform.UIKit.UISceneConnectionOptions


fun MainViewController(args: LaunchArguments) = ComposeUIViewController { App(args) }


object AppDelegateKotlin {

    @Composable
    fun collectLaunchArgumentsAsState() = launchArguments.collectAsState()


    fun getInitialArguments(): LaunchArguments = launchArguments.value

    fun applicationDidFinishLaunching() {
        AppShortcuts.reset()
    }

    fun applicationDidFinishLaunching(
        options: Map<UIApplicationLaunchOptionsKey, *>?
    ): Boolean {
        AppShortcuts.reset()
        launchArguments.value = LaunchArguments.gatherInitial(options)
        return true
    }

    fun applicationLaunchedForShortcutItem(
        item: UIApplicationShortcutItem,
        completionHandler: (Boolean) -> Unit
    ) {
        launchArguments.value = launchArguments.value
            .overwriteInitialRemoteServerUsing(shortcutItem = item)

        completionHandler(true)
    }

}

object SceneDelegateKotlin {

    fun sceneWillConnectToWithOptions(
        options: UISceneConnectionOptions
    ) {
        options.shortcutItem?.run {
            launchArguments.value = launchArguments.value
                .overwriteInitialRemoteServerUsing(shortcutItem = this)
        }
    }

    fun windowScenePerformActionFor(
        performActionFor: UIApplicationShortcutItem,
        completionHandler: (Boolean) -> Unit
    ) {
        launchArguments.value = launchArguments.value
            .overwriteInitialRemoteServerUsing(shortcutItem = performActionFor)

        completionHandler(true)
    }

}


private val launchArguments = MutableStateFlow(LaunchArguments())

private fun LaunchArguments.Companion.gatherInitial(options: Map<UIApplicationLaunchOptionsKey, *>?): LaunchArguments {
    return LaunchArguments(
        initialRemoteServer = getInitialRemoteServer(options.retrieveShortcutItem())
    )
}

private fun LaunchArguments.overwriteInitialRemoteServerUsing(shortcutItem: UIApplicationShortcutItem) =
    copy(initialRemoteServer = getInitialRemoteServer(shortcutItem))

private fun getInitialRemoteServer(shortcutItem: UIApplicationShortcutItem?): RemoteServer? {
    shortcutItem ?: return null

    val ident = shortcutItem.type.removePrefix("initially_connect_").toIntOrNull() ?: return null

    val store = createUserSpaceStore()
    val servers = RemoteServerMutation.loadFromStore(store)

    return servers.find { it.ident == ident }
}

private fun Map<UIApplicationLaunchOptionsKey, *>?.retrieveShortcutItem(): UIApplicationShortcutItem? =
    this?.get(UIApplicationLaunchOptionsShortcutItemKey) as? UIApplicationShortcutItem

