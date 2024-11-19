package kiwi.hoonkun.ckremote.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import kiwi.hoonkun.ckremote.core.player.RemoteServer
import kiwi.hoonkun.ckremote.core.player.RemoteServerMutation
import kiwi.hoonkun.ckremote.utils.createUserSpaceStore
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationShortcutIcon
import platform.UIKit.UIApplicationShortcutItem
import platform.UIKit.shortcutItems


@Composable
actual fun AppShortcutApplyEffect() {
    val mutated by RemoteServerMutation.collectAsState()

    SideEffect {
        mutated?.run { AppShortcuts.update(this) }
    }
}

object AppShortcuts {

    fun reset() {
        val store = createUserSpaceStore()
        val servers = RemoteServerMutation.loadFromStore(store)

        update(servers)
    }

    fun update(servers: List<RemoteServer>) {
        UIApplication.sharedApplication().shortcutItems = servers.map {
            UIApplicationShortcutItem(
                type = "initially_connect_${it.ident}",
                localizedTitle = it.name,
                localizedSubtitle = "바로 연결하기",
                icon = UIApplicationShortcutIcon.iconWithSystemImageName("play"),
                userInfo = mapOf("ident" to it.ident)
            )
        }
    }

}