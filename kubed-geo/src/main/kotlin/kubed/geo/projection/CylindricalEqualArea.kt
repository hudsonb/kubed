package kubed.geo.projection

import kubed.geo.math.asin
import kotlin.math.cos
import kotlin.math.sin

class CylindricalEqualArea(phi0: Double) : InvertableProjector {
    private val cosPhi0 = cos(phi0)

    override fun invoke(lambda: Double, phi: Double): DoubleArray = doubleArrayOf(lambda * cosPhi0, sin(phi) / cosPhi0)

    override fun invert(x: Double, y: Double): DoubleArray = doubleArrayOf(x / cosPhi0, asin(y * cosPhi0))
}