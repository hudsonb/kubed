package kubed.interpolate

import kubed.color.Rgb

fun interpolateRgb(start: Any, end: Any, gamma: Double = 1.0): (Double) -> Rgb {
    val sc = Rgb.convert(start)
    val ec = Rgb.convert(end)

    val r = gamma(gamma)(sc.r.toDouble(), ec.r.toDouble())
    val g = gamma(gamma)(sc.g.toDouble(), ec.g.toDouble())
    val b = gamma(gamma)(sc.b.toDouble(), ec.b.toDouble())
    val opacity = nogamma(sc.opacity, ec.opacity)

    return {t -> Rgb(r(t).toInt(), g(t).toInt(), b(t).toInt(), opacity(t))}
}

fun interpolateRgbBasis(vararg colors: Any) = interpolateRgb(colors, ::basis)
fun interpolateRgbBasisClosed(vararg colors: Any) = interpolateRgb(colors, ::basisClosed)

private fun interpolateRgb(vararg colors: Any, spline: (DoubleArray) -> (Double) -> Double): (Double) -> Rgb {
    val n = colors.size
    val r = DoubleArray(n)
    val g = DoubleArray(n)
    val b = DoubleArray(n)

    for(i in colors.indices) {
        val rgb = Rgb.convert(colors[i])
        r[i] = rgb.r.toDouble()
        g[i] = rgb.g.toDouble()
        b[i] = rgb.b.toDouble()
    }

    val rs = spline(r)
    val gs = spline(g)
    val bs = spline(b)

    return { t -> Rgb(rs(t).toInt(), gs(t).toInt(), bs(t).toInt(), 1.0) }
}
