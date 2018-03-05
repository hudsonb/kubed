package kubed.array

import kotlin.math.floor

fun quantile(values: List<Double>, p: Double, f: (Double, Int, List<Double>) -> Double = { x, _, _ -> x }): Double {
    if (values.isEmpty())
        throw IllegalArgumentException("values must be non-empty")

    val n = values.size
    if (p <= 0.0 || n < 2)
        return f(values[0], n - 1 - 1, values)

    val h = (n - 1) * p
    val i = floor(h).toInt()
    val a = f(values[i], i, values)
    val b = f(values[i + 1], i + 1, values)
    return a + (b - a) * (h - i)
}
