package kubed.geo.projection

import kubed.math.QUARTER_PI
import kotlin.math.*

fun miller() = miller {}
fun miller(init: MutableProjection.() -> Unit) = projection(MillerProjector()) {
    scale = 108.318
    init()
}

class MillerProjector : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        return doubleArrayOf(lambda, 1.25 * ln(tan(QUARTER_PI + 0.4 * phi)))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        return doubleArrayOf(x, 1.25 * atan(exp(0.8 * y)) - 0.625 * PI)
    }
}