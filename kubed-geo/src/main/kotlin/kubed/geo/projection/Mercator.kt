package kubed.geo.projection

import javafx.geometry.Rectangle2D
import kubed.math.HALF_PI
import kubed.math.TAU
import kotlin.math.*

fun mercator() = mercator {}
fun mercator(init: MercatorProjection.() -> Unit) = mercatorProjection(MercatorProjector()) {
    scale = 961 / TAU
    init()
}

fun mercatorProjection(projector: Projector) = mercatorProjection(projector) {}
fun mercatorProjection(projector: Projector, init: MercatorProjection.() -> Unit) = MercatorProjection(projector).apply(init)

class MercatorProjector : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray = doubleArrayOf(lambda, ln(tan((HALF_PI + phi) / 2)))
    override fun invert(x: Double, y: Double): DoubleArray = doubleArrayOf(x, 2 * atan(exp(y)) - HALF_PI)
}

open class MercatorProjection(projector: Projector) : MutableProjection(projector) {
    private var reclipping = false

    init {
        scaleProperty.addListener { _ -> reclip() }
        translateXProperty.addListener { _ -> reclip() }
        translateYProperty.addListener { _ -> reclip() }
        centerProperty.addListener { _ -> reclip() }
        clipExtentProperty.addListener { _ -> if(!reclipping) reclip() }
    }

    private fun reclip() {
        reclipping = true

        val k = PI * scale
        val t = invoke(rotation(rotateX, rotateY, rotateZ).invert(0.0, 0.0))

        val e = clipExtent
        val extent = when {
                    e == null -> Rectangle2D(t[0] - k, t[1] - k, k * 2, k * 2)
                    projector is MercatorProjector -> Rectangle2D(max(t[0] - k, e.minX), e.minY, max(0.0, min(k * 2, e.width)), e.height)
                    else -> Rectangle2D(e.minX, max(t[1] - k, e.minY), e.width, min(k * 2, e.height))
                }

        clipExtent = extent
        reclipping = false
    }
}