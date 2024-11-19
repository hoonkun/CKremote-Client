package kiwi.hoonkun.ckremote.ui.overlay

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kiwi.hoonkun.ckremote.utils.compose.nullIndicationClickable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource


@Composable
fun ActionsMenu(
    hasActiveServer: Boolean,
    trueContent: @Composable ColumnScope.() -> Unit,
    falseContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier
) {

    AnimatedContent(
        targetState = hasActiveServer,
        transitionSpec = {
            val enter = if (targetState) fadeIn(tween(delayMillis = 500)) else fadeIn() + slideInHorizontally { it / 6 }
            val exit = fadeOut() + slideOutHorizontally { it / 6 }
            enter togetherWith exit
        },
        modifier = modifier
    ) { capturedHasActiveServer ->

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 30.dp)
        ) {
            if (capturedHasActiveServer) {
                trueContent()
            } else {
                falseContent()
            }
        }
    }

}


@Composable
fun ActionsMenuItem(
    text: String,
    icon: DrawableResource,
    iconScale: Float = 1.0f,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .nullIndicationClickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp).scale(iconScale)
        )
    }
}
