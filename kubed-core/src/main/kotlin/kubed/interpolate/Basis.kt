package kubed.interpolate

private fun basis(t1: Double, v0: Double, v1: Double, v2: Double, v3: Double): Double {
    val t2 = t1 * t1
    val t3 = t2 * t1
    return ( (1.0 - 3.0 * t1 + 3.0 * t2 - t3) * v0
           + (4.0 - 6.0 * t2 + 3.0 * t3) * v1
           + (1.0 + 3.0 * t1 + 3.0 * t2 - 3.0 * t3) * v2
           + t3 * v3) / 6.0
}

internal fun basis(vararg values: Double): (Double) -> Double {
    val n = values.size - 1
    return { t ->
        val i: Int
        val t1: Double
        when {
            t <= 0 -> {
                t1 = 0.0
                i = 0
            }
            t >= 1 -> {
                t1 = 1.0
                i = n - 1
            }
            else -> {
                t1 = t
                i =  Math.floor(t * n).toInt()
            }
        }
        val v1 = values[i]
        val v2 = values[i + 1]
        val v0 = if(i > 0) values[i - 1] else 2 * v1 - v2
        val v3 = if(i < n - 1) values[i + 2] else 2 * v2 - v1
        basis((t1 - i / n.toDouble()) * n, v0, v1, v2, v3)
    }
}

internal fun basisClosed(vararg values: Double): (Double) -> Double {
    val n = values.size
    return { t ->
        var t1 = t % 1.0
        val i = Math.floor(((if(t1 < 0) ++t1 else t1)) * n)
        val v0 = values[((i + n - 1) % n).toInt()]
        val v1 = values[(i % n).toInt()]
        val v2 = values[((i + 1) % n).toInt()]
        val v3 = values[((i + 2) % n).toInt()]
        basis((t - i / n) * n, v0, v1, v2, v3)
    }
}