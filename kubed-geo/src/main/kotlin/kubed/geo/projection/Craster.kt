package kubed.geo.projection

import kubed.geo.math.SQRT3
import kubed.geo.math.SQRT_PI
import kubed.geo.math.asin
import kotlin.math.cos
import kotlin.math.sin

fun craster() = craster {}
fun craster(init: MutableProjection.() -> Unit) = projection(CrasterProjector()) {
    scale = 156.19
    init()
}

class CrasterProjector : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        return doubleArrayOf(SQRT3 * lambda * (2 * cos(2 * phi / 3) - 1) / SQRT_PI,
                             SQRT3 * SQRT_PI * sin(phi / 3))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val phi = 3 * asin(y / (SQRT3 * SQRT_PI))
        return doubleArrayOf(SQRT_PI * x / (SQRT3 * (2 * cos(2 * phi / 3) - 1)), phi)
    }
}