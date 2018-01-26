package kubed.geo.math

import kubed.math.HALF_PI
import kubed.util.isTruthy
import kotlin.math.PI
import kotlin.math.sin

inline fun acos(x: Double) = when {
    x > 1 -> 0.0
    x < -1 -> PI
    else -> kotlin.math.acos(x)
}

inline fun asin(x: Double) = when {
    x > 1 -> HALF_PI
    x < -1 -> -HALF_PI
    else -> kotlin.math.asin(x)
}

inline fun sinci(x: Double) = if(x.isTruthy()) x / sin(x) else 1.0