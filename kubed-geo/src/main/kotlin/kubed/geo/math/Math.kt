package kubed.geo.math

import kubed.math.HALF_PI
import kubed.util.isTruthy
import kotlin.math.*

val SQRT2 = sqrt(2.0)
val SQRT3 = sqrt(3.0)
val SQRT_PI = sqrt(PI)

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

inline fun arsinh(x: Double) = ln(x + sqrt(x * x + 1))

inline fun arcosh(x: Double) = ln(x + sqrt(x * x - 1))

inline fun tany(y: Double): Double = tan((HALF_PI + y) / 2)