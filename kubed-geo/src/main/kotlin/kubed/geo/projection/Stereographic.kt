package kubed.geo.projection

import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin

fun stereographic() = stereographic {}
fun stereographic(init: Projection.() -> Unit) = projection(StereographicProjector()) {
    scale = 250.0
    clipAngle = 142.0

    init()
}
class StereographicProjector : InvertableProjector {
    private val azimuthalInvert = azimuthalInvert { z -> 2 * atan(z) }

    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val cy = cos(phi)
        val k = 1 + cos(lambda) * cy
        return doubleArrayOf(cy * sin(lambda) / k, sin(phi) / k)
    }

    override fun invert(x: Double, y: Double) = azimuthalInvert(x, y)
}