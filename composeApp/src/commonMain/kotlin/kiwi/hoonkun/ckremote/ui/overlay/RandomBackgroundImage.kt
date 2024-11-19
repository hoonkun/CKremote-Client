package kiwi.hoonkun.ckremote.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import ckremote.composeapp.generated.resources.Res
import ckremote.composeapp.generated.resources.allDrawableResources
import ckremote.composeapp.generated.resources.initial_background_2
import kiwi.hoonkun.ckremote.utils.DateBasedRandom
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import kotlin.random.nextInt


@OptIn(ExperimentalResourceApi::class)
@Composable
fun RandomBackground(visible: Boolean) {

    val randomBackground = remember {
        val index = DateBasedRandom(60 * 60).nextInt(0..8)
        Res.allDrawableResources["initial_background_$index"] ?: Res.drawable.initial_background_2
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Image(
            painter = painterResource(randomBackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }

}
