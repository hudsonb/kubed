package kubed.geo.projection

import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin

fun gnomonic() = gnomonic {}
fun gnomonic(init: MutableProjection.() -> Unit) = projection(GnomonicProjector()) {
    scale = 144.049
    clipAngle = 60.0

    init()
}

class GnomonicProjector : InvertableProjector {
    private val invert = azimuthalInvert(::atan)

    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val cy = cos(phi)
        val k = cos(lambda) * cy
        return doubleArrayOf(cy * sin(lambda) / k, sin(phi) / k)
    }

    override fun invert(x: Double, y: Double) = invert.invoke(x, y)
}