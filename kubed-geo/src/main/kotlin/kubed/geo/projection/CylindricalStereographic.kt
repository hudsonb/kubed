package kubed.geo.projection

import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.tan

fun cylindricalStereographic() = cylindricalStereographic {}
fun cylindricalStereographic(init: Parallel1Projection.() -> Unit) = Parallel1Projection(::CylindricalStereographicProjector).apply {
    scale = 124.75
    init()
}

fun gallsStereographic() = gallsStereographic {}
fun gallsStereographic(init: Parallel1Projection.() -> Unit) = cylindricalStereographic {
    parallel = 45.0
    init()
}

class CylindricalStereographicProjector(phi0: Double) : InvertableProjector {
    private val cosPhi0 = cos(phi0)

    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        return doubleArrayOf(lambda * cosPhi0,
                (1 + cosPhi0) * tan(phi / 2))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        return doubleArrayOf(x / cosPhi0,
                             atan(y / (1 + cosPhi0)) * 2)
    }
}