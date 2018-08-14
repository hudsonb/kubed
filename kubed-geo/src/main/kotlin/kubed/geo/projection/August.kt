package kubed.geo.projection

import kubed.geo.math.arcosh
import kubed.geo.math.arsinh
import kubed.util.isFalsy
import kubed.geo.math.sqrt
import kubed.math.asin
import kubed.util.isTruthy
import kotlin.math.*

fun august() = august {}
fun august(init: MutableProjection.() -> Unit) = projection(AugustProjector()) {
    scale = 66.1603

    init()
}

class AugustProjector : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val tanPhi = tan(phi / 2)
        val k = sqrt(1 - tanPhi * tanPhi)
        val lambda2 = lambda / 2
        val c = 1 + k * cos(lambda2)
        val x = sin(lambda2) * k / c
        val y = tanPhi / c
        val x2 = x * x
        val y2 = y * y
        return doubleArrayOf(4.0 / 3.0 * x * (3 + x2 - 3 * y2),
                             4.0 / 3.0 * y * (3 + 3 * x2 - y2))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val x1 = x * 3 / 8
        val y1 = y * 3 / 8

        if(x1.isFalsy() && abs(y1) > 1) throw IllegalStateException()

        val x2 = x1 * x1
        val y2 = y1 * y1
        val s = 1 + x2 + y2
        val sin3Eta = sqrt((s - sqrt(s * s - 4 * y1 * y1)) / 2)
        val eta = asin(sin3Eta) / 3
        val xi = if(sin3Eta.isTruthy()) arcosh(y1 / sin3Eta) / 3 else arsinh(abs(x1)) / 3
        val cosEta = cos(eta)
        val coshXi = cosh(xi)
        val d = coshXi * coshXi - cosEta * cosEta

        return doubleArrayOf(sign(x1) * 2 * atan2(sinh(xi) * cosEta, 0.25 - d),
                             sign(y1) * 2 * atan2(coshXi * sin(eta), 0.25 + d))
    }
}