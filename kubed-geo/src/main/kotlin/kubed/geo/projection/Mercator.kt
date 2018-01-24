package kubed.geo.projection

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

open class MercatorProjection(projector: Projector) : Projection(projector) {
    private var x0 = Double.NaN
    private var y0 = Double.NaN
    private var x1 = Double.NaN
    private var y1 = Double.NaN

    override var scale: Double
        get() = super.scale
        set(value) {
            super.scale = value
            reclip()
        }

    override var translate: DoubleArray
        get() = super.translate
        set(value) {
            super.translate = value
            reclip()
        }

    override var center: DoubleArray
        get() = super.center
        set(value) {
            super.center = value
            reclip()
        }

    override var clipExtent: Array<DoubleArray>?
        get() = if(x0.isNaN()) null else arrayOf(doubleArrayOf(x0, y0), doubleArrayOf(x1, y1))
        set(value) {
            if(value == null) {
                x0 = Double.NaN
                y0 = Double.NaN
                x1 = Double.NaN
                y1 = Double.NaN
            }
            else {
                x0 = value[0][0]
                y0 = value[0][1]
                x1 = value[1][0]
                y1 = value[1][1]
            }

            reclip()
        }

    private fun reclip() {
        val k = PI * scale
        val t = super.invoke(rotation(rotate).invert(0.0, 0.0))

        val e = when {
                    x0.isNaN() -> arrayOf(doubleArrayOf(t[0] - k, t[1] - k), doubleArrayOf(t[0] + k, t[1] + k))
                    project::class == MercatorProjector::class -> arrayOf(doubleArrayOf(max(t[0] - k, x0), y0), doubleArrayOf(min(t[0] + k, x1), y1))
                    else -> arrayOf(doubleArrayOf(x0, max(t[1] - k, y0)), doubleArrayOf(x1, min(t[1] + k, y1)))
                }

        super.clipExtent = e
    }
}