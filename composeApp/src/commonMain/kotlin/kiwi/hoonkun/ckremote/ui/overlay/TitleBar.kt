package kiwi.hoonkun.ckremote.ui.overlay

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ckremote.composeapp.generated.resources.Res
import ckremote.composeapp.generated.resources.icon_original
import kiwi.hoonkun.ckremote.utils.compose.offset
import org.jetbrains.compose.resources.painterResource


@Composable
fun TitleBar() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = 8.dp)
            .height(60.dp)
    ) {
        Image(
            painter = painterResource(Res.drawable.icon_original),
            contentDescription = null,
            modifier = Modifier.padding(end = 16.dp).size(28.dp)
        )
        Box(
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = "코어키퍼 리모트 플레이어 ",
                fontSize = 20.sp
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.2f))) {
                        append("by ")
                    }
                    withStyle(SpanStyle(brush = CreatorColorGradient, alpha = 0.35f)) {
                        append("@ddunkun_")
                    }
                },
                fontSize = 12.sp,
                modifier = Modifier.offset(y = 1f).offset(y = (-2).dp)
            )
        }
    }
}

private val CreatorColorGradient = Brush.horizontalGradient(
    0.0f to Color(0xffcaff8a),
    0.4f to Color(0xffcaff8a),
    0.8f to Color(0xffffbb8a),
    1.0f to Color(0xffffbb8a)
)
