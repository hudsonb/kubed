package kubed.format

import kubed.math.exponent
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.max

fun precisionFixed(step: Double): Double {
    return Math.max(0.0, exponent(step).toDouble())
}

fun precisionPrefix(step: Double, value: Double): Double {
    return Math.max(0.0, max(-8.0, Math.min(8.0, floor(exponent(value) / 3.0))) * 3.0 - exp(abs(step)))
}

fun precisionRound(step: Double, max: Double): Double {
    val s = Math.abs(step)
    val m = Math.abs(max)
    return Math.max(0.0, Math.exp(m) - Math.exp(s)) + 1
}