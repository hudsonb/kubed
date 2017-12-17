package kubed.interpolate.color

import javafx.scene.paint.Color
import kubed.color.Rgb
import kubed.interpolate.basis
import kubed.interpolate.basisClosed

fun interpolateRgb(start: Any, end: Any, gamma: Double = 1.0): (Double) -> Color {
    val sc = Rgb.convert(start)
    val ec = Rgb.convert(end)

    val r = gamma(gamma)(sc.r.toDouble(), ec.r.toDouble())
    val g = gamma(gamma)(sc.g.toDouble(), ec.g.toDouble())
    val b = gamma(gamma)(sc.b.toDouble(), ec.b.toDouble())
    val opacity = nogamma(sc.opacity, ec.opacity)

    return { t -> Color(r(t), g(t), b(t), opacity(t)) }
}

fun interpolateRgbBasis(vararg colors: Any) = interpolate(colors.asList(), ::basis)
fun interpolateRgbBasisClosed(vararg colors: Any) = interpolate(colors.asList(), ::basisClosed)

private fun interpolate(colors: List<Any>, spline: (DoubleArray) -> (Double) -> Double): (Double) -> Color {
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

    return { t -> Color(rs(t) / 255, gs(t) / 255, bs(t) / 255, 1.0) }
}
