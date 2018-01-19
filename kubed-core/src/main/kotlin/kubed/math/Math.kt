package kubed.math

import kubed.format.formatDecimal
import java.text.DecimalFormat
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

const val HALF_PI = PI * .5
const val QUARTER_PI = PI * .25
const val TAU = PI * 2

const val DEGREES = 180.0 / PI
const val RADIANS = PI / 180.0

const val EPSILON = 1e-6
const val TAU_EPSILON = TAU - EPSILON

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

inline fun haversin(x: Double): Double {
    val xs = sin(x / 2)
    return xs * xs
}

fun map(value: Double, istart: Double, istop: Double, ostart: Double, ostop: Double): Double {
    return ostart + (ostop - ostart) * ((value - istart) / (istop - istart))
}

fun exponent(x: Double): Int {
    val f = formatDecimal(abs(x))
    return f.exponent
}

/**
 * Linear interpolation between [min] and [max] at the given
 * [ratio].
 * Returns the interpolated value in the interval `[min;max]`.
 *
 * @param min
 * The lower interval bound.
 *
 * @param max
 * The upper interval bound.
 *
 * @param ratio
 * A value in the interval `[0;1]`.
 *
 * @return The interpolated value.
 */
fun lerp(min: Double, max: Double, ratio: Double): Double {
    val d = (1 - ratio) * min + ratio * max
    return if(d.isNaN()) 0.0 else Math.min(max, Math.max(min, d))
}

/**
 * Normalizes a given [value] which is in range `[min;max]`
 * to range `[0;1]`.
 *
 * @param min
 * The lower bound of the range.
 *
 * @param max
 * The upper bound of the range.
 *
 * @param value
 * The value in the range.
 *
 * @return The normalized value (in range `[0;1]`).
 */
fun norm(min: Double, max: Double, value: Double): Double {
    val d = (value - min) / (max - min)
    return if(d.isNaN()) 0.0 else Math.min(1.0, Math.max(0.0, d))
}


fun Number.toExponential(p: Int = 20): String {
    var format = "0."

    for(i in 0..p)
        format += "#"

    format += "E0"

    val f = DecimalFormat(format)
    return f.format(this)
}


/**
 * Assuming this represents a value in degrees, converts the value to radians.
 */
fun Double.toRadians() = this * RADIANS

/**
 * Assuming this represents a value in radians, converts the value to degrees.
 */
fun Double.toDegrees() = this * DEGREES
