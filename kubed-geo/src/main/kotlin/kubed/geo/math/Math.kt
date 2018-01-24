package kubed.geo.math

import kubed.math.HALF_PI
import kotlin.math.PI

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