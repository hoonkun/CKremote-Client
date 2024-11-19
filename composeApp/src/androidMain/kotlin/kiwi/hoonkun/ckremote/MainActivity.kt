package kiwi.hoonkun.ckremote

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kiwi.hoonkun.ckremote.core.AppShortcuts
import kiwi.hoonkun.ckremote.core.player.RemoteServer
import kiwi.hoonkun.ckremote.core.player.RemoteServerMutation
import kiwi.hoonkun.ckremote.core.player.initializeWebRTC
import kiwi.hoonkun.ckremote.utils.createUserSpaceStore
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val launchArguments = MutableStateFlow(LaunchArguments())

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        super.onCreate(savedInstanceState)

        initializeWebRTC(applicationContext)

        val launchArgs = LaunchArguments.gatherInitial().also { launchArguments.value = it }
        AppShortcuts.reset(this)

        setContent { App(launchArgs) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        launchArguments.value = launchArguments.value.overwriteInitialRemoteServerUsing(intent)
    }

    @Composable
    fun collectLaunchArgumentsAsState(initialArguments: LaunchArguments) =
        launchArguments.collectAsState(initial = initialArguments)

    private fun LaunchArguments.Companion.gatherInitial(
        intent: Intent? = this@MainActivity.intent
    ): LaunchArguments {
        return LaunchArguments(
            initialRemoteServer = getInitialRemoteServer(intent)
        )
    }

    private fun LaunchArguments.overwriteInitialRemoteServerUsing(intent: Intent) =
        copy(initialRemoteServer = getInitialRemoteServer(intent))

    private fun getInitialRemoteServer(intent: Intent?): RemoteServer? {
        val action = AppShortcuts.INTENT_SHORTCUT_LAUNCH_ACTION

        val capturedIntent = intent ?: return null
        if (capturedIntent.action != action) return null

        val ident = capturedIntent.getIntExtra("ident", -1).takeIf { it != -1 } ?: return null

        val store = createUserSpaceStore()
        val servers = RemoteServerMutation.loadFromStore(store)

        return servers.find { it.ident == ident }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsets.Type.systemBars())
            }
        } else {
            @Suppress("DEPRECATION")
            val flags = listOf(
                View.SYSTEM_UI_FLAG_IMMERSIVE,
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE,
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION,
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN,
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION,
                View.SYSTEM_UI_FLAG_FULLSCREEN
            )

            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = flags.fold(0) { acc, curr -> acc or curr }
        }
    }

}