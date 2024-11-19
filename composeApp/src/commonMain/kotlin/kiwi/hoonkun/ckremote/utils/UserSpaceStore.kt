package kiwi.hoonkun.ckremote.utils

import androidx.compose.runtime.Composable


interface UserSpaceStore {
    operator fun set(key: String, value: String)
    operator fun get(key: String): String?
}

@Composable
expect fun rememberUserSpaceStore(): UserSpaceStore