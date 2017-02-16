package kubed.interpolate

fun interpolateNumber(a: Double, b: Double): (Double) -> Double {
    val d = b - a
    return { t -> a + d * t }
}