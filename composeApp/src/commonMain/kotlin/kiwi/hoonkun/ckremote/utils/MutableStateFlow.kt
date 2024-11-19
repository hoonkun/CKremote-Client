package kiwi.hoonkun.ckremote.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow


@Composable
fun <T>MutableStateFlow<T>.collectAsState(): State<T> = collectAsState(value)