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

    private var userClipExtent: Rectangle2D? = null

    override var scale
        get() = super.scale
        set(value) {
            super.scale = value
            reclip()
        }

    override var translateX
        get() = super.translateX
        set(value) {
            super.translateX = value
            reclip()
        }

    override var translateY
        get() = super.translateY
        set(value) {
            super.translateY = value
            reclip()
        }

    override var center
        get() = super.center
        set(value) {
            super.center = value
            reclip()
        }

    override var clipExtent
        get() = userClipExtent
        set(value) {
            userClipExtent = value
            reclip()
        }

    private fun reclip() {
        val k = PI * scale
        val t = invoke(rotation(rotateX, rotateY, rotateZ).invert(0.0, 0.0))

        val e = userClipExtent
        val extent = when {
            e == null -> Rectangle2D(t[0] - k, t[1] - k, max(1.0, k * 2), max(1.0, k * 2))
            projector is MercatorProjector -> Rectangle2D(max(t[0] - k, e.minX), e.minY, max(0.0, min(k * 2, e.width)), e.height)
            else -> Rectangle2D(e.minX, max(t[1] - k, e.minY), e.width, min(k * 2, e.height))
        }

        super.clipExtent = extent
    }
}