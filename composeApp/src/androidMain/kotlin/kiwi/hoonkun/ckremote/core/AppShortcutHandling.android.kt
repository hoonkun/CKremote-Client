package kiwi.hoonkun.ckremote.core

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import kiwi.hoonkun.ckremote.MainActivity
import kiwi.hoonkun.ckremote.R
import kiwi.hoonkun.ckremote.core.player.RemoteServer
import kiwi.hoonkun.ckremote.core.player.RemoteServerMutation
import kiwi.hoonkun.ckremote.utils.createUserSpaceStore

@Composable
actual fun AppShortcutApplyEffect() {
    val context = LocalContext.current
    val mutated by RemoteServerMutation.collectAsState()

    SideEffect {
        mutated?.run { AppShortcuts.update(context, this) }
    }
}

object AppShortcuts {

    const val INTENT_SHORTCUT_LAUNCH_ACTION = "SHORTCUT_LAUNCH_WITH_SERVER"

    fun reset(context: Context) {
        val store = context.createUserSpaceStore()
        val servers = RemoteServerMutation.loadFromStore(store)

        update(context, servers)
    }

    fun update(context: Context, servers: List<RemoteServer>) {
        ShortcutManagerCompat.removeAllDynamicShortcuts(context)

        servers
            .map {
                val intent = Intent(context, MainActivity::class.java).apply {
                    setAction(INTENT_SHORTCUT_LAUNCH_ACTION)
                    putExtra("ident", it.ident)
                }

                ShortcutInfoCompat.Builder(context, "initially_connect_${it.ident}")
                    .setShortLabel(it.name)
                    .setLongLabel("${it.name} 연결")
                    .setIcon(IconCompat.createWithResource(context, R.drawable.play))
                    .setIntent(intent)
                    .build()
            }.forEach {
                ShortcutManagerCompat.pushDynamicShortcut(context, it)
            }
    }

}