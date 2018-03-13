package kubed.geo.projection

import kubed.geo.math.sqrt
import kubed.math.EPSILON
import kubed.math.HALF_PI
import kubed.math.QUARTER_PI
import kotlin.math.*

fun baker() = baker {}
fun baker(init: MutableProjection.() -> Unit) = projection(BakerProjector()) {
    scale = 112.314

    init()
}

/**
 * The Baker Dinomic projection.
 */
class BakerProjector : InvertableProjector {
    private val sqrt2 = sqrt(2.0)
    private val sqrt8 = sqrt(8.0)
    private val phi0 = ln(1 + sqrt2)

    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val phi0 = abs(phi)
        return when {
            phi0 < QUARTER_PI -> doubleArrayOf(lambda, ln(tan(QUARTER_PI + phi / 2)))
            else -> doubleArrayOf(lambda * cos(phi0) * (2 * sqrt2 - 1 / sin(phi0)),
                                  sign(phi) * (2 * sqrt2 * (phi0 - QUARTER_PI) - ln(tan(phi0 / 2))))
        }
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val y0 = abs(y)
        if(y0 < phi0) return doubleArrayOf(x, 2 * atan(exp(y)) - HALF_PI)

        var phi = QUARTER_PI
        var i = 25
        do {
            val cosPhi2 = cos(phi / 2)
            val tanPhi2 = tan(phi / 2)
            val delta = (sqrt8 * (phi - QUARTER_PI) - ln(tanPhi2) - y0) / (sqrt8 - cosPhi2 * cosPhi2 / (2 * tanPhi2))
            phi -= delta
        } while(abs(delta) > EPSILON * 2 && --i > 0.0)

        return doubleArrayOf(x / (cos(phi) * (sqrt8 - 1 / sin(phi))), sign(y) * phi)
    }
}