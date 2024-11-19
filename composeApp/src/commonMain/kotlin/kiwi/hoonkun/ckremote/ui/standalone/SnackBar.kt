package kiwi.hoonkun.ckremote.ui.standalone

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kiwi.hoonkun.ckremote.utils.compose.nullIndicationClickable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Stable
object SnackBar {

    val items = mutableStateListOf<Item>()
    private val scope = CoroutineScope(Dispatchers.Default)

    @OptIn(ExperimentalUuidApi::class)
    fun make(
        autoHideDuration: Long = 3000,
        content: @Composable BoxScope.(String) -> Unit
    ): String {
        val ident = Uuid.random().toHexString()
        val item = Item(ident, autoHideDuration, content)
        items.add(item)

        if (autoHideDuration > 0) {
            scope.launch {
                delay(autoHideDuration)
                destroy(ident)
            }
        }

        return ident
    }

    fun destroy(
        ident: String
    ) {
        items.remove(items.find { it.ident == ident })
    }


    @Stable
    data class Item(
        val ident: String,
        val autoHideDuration: Long,
        val content: @Composable BoxScope.(String) -> Unit
    ) {
        var state by mutableStateOf(0)
    }

}

@Composable
fun BoxScope.SnackBars() {
    LazyColumn(
        horizontalAlignment = Alignment.End,
        userScrollEnabled = false,
        modifier = Modifier.align(Alignment.TopEnd).offset(y = 64.dp)
    ) {
        items(items = SnackBar.items, key = { it.ident }) {
            val offset by animateDpAsState(if (it.state == 1) 0.dp else 24.dp)

            SideEffect { it.state = 1 }

            CompositionLocalProvider(
                LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 13.sp)
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer { translationX = offset.toPx() }
                        .background(Color(0xff202020).copy(alpha = 0.9f))
                        .padding(top = 8.dp, start = 24.dp, bottom = 8.dp, end = 56.dp)
                        .animateContentSize()
                        .animateItem()
                ) {
                    it.content(this, it.ident)
                }
            }
        }
    }
}

@Composable
fun SnackBarAction(
    parentIdent: String,
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = Color(0xff66a8f5),
        modifier = Modifier
            .padding(start = 16.dp)
            .nullIndicationClickable {
                onClick()
                SnackBar.destroy(parentIdent)
            }
    )
}
