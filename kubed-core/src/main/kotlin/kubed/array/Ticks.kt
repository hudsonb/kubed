package kubed.array

import java.lang.Math.pow
import kotlin.math.*

val E10 = sqrt(50.0)
val E5 = sqrt(10.0)
val E2 = sqrt(2.0)
val LN10 = ln(10.0)

fun range(start: Double, stop: Double, step: Double): DoubleArray {
    val n = max(0.0, ceil((stop - start) / step)).toInt()
    return DoubleArray(n, {start + it * step })
}

fun ticks(start: Double, stop: Double, count: Int): List<Double> {
    val step = tickStep(start, stop, count)
    return range(ceil(start / step) * step,
                 floor(stop / step) * step + step / 2, // inclusive
                 step).asList()
}

fun tickStep(start: Double, stop: Double, count: Int): Double {
    if(count <= 0) throw IllegalArgumentException("count must be > 0")

    val step0 = abs(stop - start) / count
    var step1 = pow(10.0, floor(ln(step0) / LN10))
    val error = step0 / step1
    if(error >= E10) step1 *= 10
    else if(error >= E5) step1 *= 5
    else if(error >= E2) step1 *= 2
    return when {
        stop < start -> -step1
        else -> step1
    }
}