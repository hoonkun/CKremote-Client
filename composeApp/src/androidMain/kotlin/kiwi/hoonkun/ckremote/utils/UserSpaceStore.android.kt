package kiwi.hoonkun.ckremote.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext


@Composable
actual fun rememberUserSpaceStore(): UserSpaceStore {
    val context = LocalContext.current

    return remember(context) { context.createUserSpaceStore() }
}

fun Context.createUserSpaceStore(): UserSpaceStore {
    val preference = getSharedPreferences("root", Context.MODE_PRIVATE)

    return object : UserSpaceStore {
        override fun set(key: String, value: String) =
            preference.edit().putString(key, value).apply()

        override fun get(key: String): String? =
            preference.getString(key, null)
    }
}
