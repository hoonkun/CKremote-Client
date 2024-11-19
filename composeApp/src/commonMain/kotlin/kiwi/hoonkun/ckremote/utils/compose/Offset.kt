package kiwi.hoonkun.ckremote.utils.compose

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


val Offset.angle: Float get() {
    val value = PI - atan2(y, x)

    return (if (value > PI) value - PI else value + PI).toFloat()
}

fun Offset.coerceInDistance(criteria: Float) =
    if (getDistance() > criteria)
        atan2(y, x).let { Offset(x = cos(it) * criteria, y = sin(it) * criteria) }
    else
        this
