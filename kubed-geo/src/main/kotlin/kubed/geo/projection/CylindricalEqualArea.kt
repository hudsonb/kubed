package kubed.geo.projection

import kubed.math.asin
import kotlin.math.cos
import kotlin.math.sin

fun cylindricalEqualArea() = cylindricalEqualArea {}
fun cylindricalEqualArea(init: Parallel1Projection.() -> Unit) = Parallel1Projection(::CylindricalEqualArea).apply {
    scale = 195.044
    parallel = 38.58
    init()
}

fun lambertCylindricalEqualArea() = lambertCylindricalEqualArea {}
fun lambertCylindricalEqualArea(init: Parallel1Projection.() -> Unit) = cylindricalEqualArea {
    parallel = 0.0
    init()
}

fun gallPeters() = gallPeters {}
fun gallPeters(init: Parallel1Projection.() -> Unit) = cylindricalEqualArea {
    parallel = 45.0
    init()
}

fun hoboDyer() = hoboDyer {}
fun hoboDyer(init: Parallel1Projection.() -> Unit) = cylindricalEqualArea {
    parallel = 37.5
    init()
}

fun tobler() = tobler {}
fun tobler(init: Parallel1Projection.() -> Unit) = cylindricalEqualArea {
    parallel = 55.6539665
    init()
}

class CylindricalEqualArea(phi0: Double) : InvertableProjector {
    private val cosPhi0 = cos(phi0)

    override fun invoke(lambda: Double, phi: Double): DoubleArray = doubleArrayOf(lambda * cosPhi0, sin(phi) / cosPhi0)

    override fun invert(x: Double, y: Double): DoubleArray = doubleArrayOf(x / cosPhi0, asin(y * cosPhi0))
}