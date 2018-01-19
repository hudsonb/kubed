package kubed.geo.projection

import kubed.math.EPSILON
import kubed.math.asin
import kotlin.math.cos
import kotlin.math.sin

fun orthographic() = orthographic {}
fun orthographic(init: Projection.() -> Unit) = projection(OrthographicProjector()) {
    scale = 249.5
    clipAngle = 90 + EPSILON

    init()
}

class OrthographicProjector : InvertableProjector {
    private val cacheInvert = azimuthalInvert(::asin)

    override fun invoke(lambda: Double, phi: Double) = doubleArrayOf(cos(phi) * sin(lambda), sin(phi))

    override fun invert(x: Double, y: Double) = cacheInvert(x, y)
}