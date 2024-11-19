package kiwi.hoonkun.ckremote.utils

import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService


@Composable
actual fun rememberWindowMetadata(): WindowMetadata {
    val context = LocalContext.current

    return remember {
        val windowManager = context.getSystemService<WindowManager>()
            ?: return@remember WindowMetadata(-1.0, -1.0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds

            WindowMetadata(
                width = bounds.width().toDouble(),
                height = bounds.height().toDouble()
            )
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            WindowMetadata(
                width = displayMetrics.widthPixels.toDouble(),
                height = displayMetrics.heightPixels.toDouble()
            )
        }
    }
}