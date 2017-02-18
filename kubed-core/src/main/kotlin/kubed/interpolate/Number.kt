package kubed.interpolate

fun interpolateNumber(a: Double, b: Double): (Double) -> Double {
    val d = b - a
    return { t -> a + d * t }
}

fun interpolateRound(a: Double, b: Double): (Double) -> Double {
    val d = b - a
    return { t -> Math.round(a + d * t).toDouble() }
}