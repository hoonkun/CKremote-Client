package kiwi.hoonkun.ckremote.utils

import kotlinx.datetime.Clock
import kotlin.random.Random


fun DateBasedRandom(units: Long) =
    Random(Clock.System.now().epochSeconds / units)