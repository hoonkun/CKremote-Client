package kiwi.hoonkun.ckremote.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults


@Composable
actual fun rememberUserSpaceStore(): UserSpaceStore = remember { createUserSpaceStore() }

fun createUserSpaceStore() = object: UserSpaceStore {
    override fun set(key: String, value: String) =
        NSUserDefaults.standardUserDefaults.setObject(value, forKey = key)

    override fun get(key: String): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(key)
}
