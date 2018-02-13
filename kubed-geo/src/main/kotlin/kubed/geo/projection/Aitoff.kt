package kubed.geo.projection

import kubed.geo.math.sinci
import kubed.math.EPSILON
import kubed.util.isFalsy
import kubed.util.isTruthy
import kotlin.math.*

fun aitoff() = aitoff {}
fun aitoff(init: MutableProjection.() -> Unit) = projection(AitoffProjector()) {
    scale = 152.63

    init()
}

class AitoffProjector : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val cosy = cos(phi)
        val x = lambda / 2
        val sincia = sinci(acos(cosy * cos(x)))
        return doubleArrayOf(2 * cosy * sin(x) * sincia, sin(phi) * sincia)
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        // Consider: What should we return in this case? Or should we throw an exception?
        if(x * x + 4 * y * y > PI * PI + EPSILON) return doubleArrayOf(0.0, 0.0)
        var x1 = x
        var y1 = y
        var i = 25
        do {
            val sinx = sin(x1)
            val sinx2 = sin(x1 / 2)
            val cosx2 = cos(x1 / 2)
            val siny = sin(y1)
            val cosy = cos(y1)
            val sin2y = sin(2 * y1)
            val sin2y2 = siny * siny
            val cos2y = cosy * cosy
            val sin2x2 = sinx2 * sinx2
            val c = 1 - cos2y * cosx2 * cosx2
            val f = if(c.isTruthy()) 1 / c else 0.0
            val e = if(c.isTruthy()) acos(cosy * cosx2) * sqrt(f) else 0.0
            val fx = 2 * e * cosy * sinx2 - x
            val fy = e * siny - y
            val dxdx = f * (cos2y * sin2x2 + e * cosy * cosx2 * sin2y2)
            val dxdy = f * (0.5 * sinx * sin2y - e * 2 * siny * sinx2)
            val dydx = f * 0.25 * (sin2y * sinx2 - e * siny * cos2y * sinx)
            val dydy = f * (sin2y2 * cosx2 + e * sin2x2 * cosy)
            val z = dxdy * dydx - dydy * dxdx
            if(z.isFalsy()) break
            val dx = (fy * dxdy - fx * dydy) / z
            val dy = (fx * dydx - fy * dxdx) / z
            x1 -= dx
            y1 -= dy
        } while((abs(dx) > EPSILON || abs(dy) > EPSILON) && --i > 0)

        return doubleArrayOf(x1, y1)
    }
}