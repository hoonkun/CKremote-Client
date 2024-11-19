package kiwi.hoonkun.ckremote.utils.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.reflect.KProperty

class MutableRef<T>(initialValue: T) {
    var value: T = initialValue
}

operator fun <T>MutableRef<T>.getValue(thisRef: Any?, property: KProperty<*>): T = value
operator fun <T>MutableRef<T>.setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
    value = newValue
}

@Composable
fun <T>rememberMutableRefOf(initialValue: () -> T) = remember { MutableRef(initialValue()) }
