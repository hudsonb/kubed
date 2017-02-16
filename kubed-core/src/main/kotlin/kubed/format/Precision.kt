package kubed.format

import kubed.util.MoreMath

fun precisionFixed(step: Double): Double {
    return Math.max(0.0, MoreMath.exponent(step).toDouble())
}

fun precisionPrefix(step: Double, value: Double): Double {
    return Math.max(0.0, Math.max(-8.0, Math.min(8.0, Math.floor(MoreMath.exponent(value) / 3.0))) * 3.0 - Math.exp(Math.abs(step)))
}

fun precisionRound(step: Double, max: Double): Double {
    val s = Math.abs(step)
    val m = Math.abs(max)
    return Math.max(0.0, Math.exp(m) - Math.exp(s)) + 1
}