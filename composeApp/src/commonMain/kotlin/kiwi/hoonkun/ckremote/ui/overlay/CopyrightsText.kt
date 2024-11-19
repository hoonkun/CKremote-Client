package kiwi.hoonkun.ckremote.ui.overlay

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun CopyrightsText() {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White.copy(alpha = 0.4f))) {
                appendLine("Unofficial CoreKeeper Remote Client")
            }
            withStyle(SpanStyle(fontSize = 6.sp)) {
                appendLine()
            }
            withStyle(SpanStyle(color = Color.White.copy(alpha = 0.7f))) {
                appendLine("©2021, CoreKeeper is a trademark or registered trademark of Pugstorm AB.")
                appendLine("Published by Fireshine Games.")
            }
        },
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
    )
}