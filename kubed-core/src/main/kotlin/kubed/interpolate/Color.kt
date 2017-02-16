package kubed.interpolate

import kubed.util.isTruthy

internal fun linear(a: Double, d: Double): (Double) -> Double = { t -> a + t * d }

internal fun exponential(a: Double, b: Double, y: Double): (Double) -> Double {
    val a2 = Math.pow(a, y)
    val b2 = Math.pow(b, y) - a2
    val y2 = 1.0 / y
    return { t -> Math.pow(a2 + t * b2, y2)}
}

internal fun hue(a: Double, b: Double): (Double) -> Double {
    val d = b - a
    return when {
        d.isTruthy() -> linear(a, if(d > 180 || d < -180) d - 360.0 * Math.round(d / 360.0) else d)
        else -> { _ -> if(a.isNaN()) b else a }
    }
}

internal fun gamma(y: Double): (Double, Double) -> (Double) -> Double {
    return when(y) {
        1.0 -> ::nogamma
        else -> { a: Double, b: Double ->
            val d = b - a
            when {
                d.isTruthy() -> exponential(a, b, y)
                else -> { _ -> if(a.isNaN()) b else a }
            }
        }
    }
}

internal fun nogamma(a: Double, b: Double): (Double) -> Double {
    val d = b - a
    return when {
        d.isTruthy() -> linear(a, d)
        else -> { _ -> if(a.isNaN()) b else a }
    }
}
