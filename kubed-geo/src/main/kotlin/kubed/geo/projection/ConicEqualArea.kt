package kubed.geo.projection

import kubed.geo.Position
import kubed.math.EPSILON
import kubed.geo.math.asin
import kotlin.math.*

fun conicEqualArea() = conicEqualArea {}
fun conicEqualArea(init: ConicProjection.() -> Unit)= conicProjection(ConicEqualAreaProjectorFactory()) {
    scale = 155.424
    center = Position(0.0, 33.6442)
    init()
}

class ConicEqualAreaProjectorFactory : ConicProjectorFactory() {
    override fun create() = conicEqualAreaRaw(phi0, phi1)
}

fun conicEqualAreaRaw(y0: Double, y1: Double): Projector {
    val sy0 = sin(y0)
    val n = (sy0 + sin(y1)) / 2
    if(abs(n) < EPSILON) return CylindricalEqualArea(y0)
    return ConicEqualArea(y0, y1)
}

class ConicEqualArea(val y0: Double, val y1: Double) : InvertableProjector {
    private val sy0 = sin(y0)
    private val n = (sy0 + sin(y1)) / 2
    private val c = 1 + sy0 * (2 * n - sy0)
    private val r0 = sqrt(c) / n

    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val r = sqrt(c - 2 * n * sin(phi)) / n

        val x = lambda * n
        return doubleArrayOf(r * sin(x), r0 - r * cos(x))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val r0y = r0 - y
        return doubleArrayOf(atan2(x, abs(r0y)) / n * sign(r0y),
                asin((c - (x * x + r0y * r0y) * n * n) / (2 * n)))
    }
}