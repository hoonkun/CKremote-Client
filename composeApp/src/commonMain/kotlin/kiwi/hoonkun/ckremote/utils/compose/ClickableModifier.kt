package kiwi.hoonkun.ckremote.utils.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier


@Composable
fun Modifier.nullIndicationClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier =
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled = enabled,
        onClick = onClick
    )

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.nullIndicationCombinedClickable(
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier =
    this.combinedClickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled = enabled,
        onLongClick = onLongClick,
        onClick = onClick
    )

@Composable
fun Modifier.blockBehindTouch() = nullIndicationClickable {  }
