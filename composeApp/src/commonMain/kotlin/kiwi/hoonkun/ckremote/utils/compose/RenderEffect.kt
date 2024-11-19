package kiwi.hoonkun.ckremote.utils.compose

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode


@Stable
fun BlurEffect(radius: Float, edgeTreatment: TileMode = TileMode.Clamp): BlurEffect? =
    if (radius == 0f) null else BlurEffect(radius, radius, edgeTreatment)