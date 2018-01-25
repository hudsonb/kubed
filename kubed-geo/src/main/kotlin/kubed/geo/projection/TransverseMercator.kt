package kubed.geo.projection

import kubed.math.HALF_PI
import kotlin.math.*

fun transverseMercator() = transverseMercator {}
fun transverseMercator(init: TransverseMercatorProjection.() -> Unit) = transverseMercatorProjection {
    rotate = doubleArrayOf(0.0, 0.0, 0.0)
    scale = 159.155
    init()
}

fun transverseMercatorProjection() = transverseMercatorProjection {}
fun transverseMercatorProjection(init: TransverseMercatorProjection.() -> Unit) = TransverseMercatorProjection().apply(init)


class TransverseMercatorProjector : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double) = doubleArrayOf(ln(tan((HALF_PI + phi) / 2)), -lambda)
    override fun invert(x: Double, y: Double) = doubleArrayOf(-y, 2 * atan(exp(x)) - HALF_PI)
}

class TransverseMercatorProjection : MercatorProjection(TransverseMercatorProjector()) {
    override var center: DoubleArray
        get() {
            val c = super.center
            return doubleArrayOf(c[1], -c[0])
        }
        set(value) {
            super.center = doubleArrayOf(-value[1], value[0])
        }

    override var rotate: DoubleArray
        get() {
            val r = super.rotate
            return doubleArrayOf(r[0], r[1], r[2] - 90)
        }
        set(r) {
            super.rotate = doubleArrayOf(r[0], r[1], if(r.size > 2) r[2] + 90.0 else 90.0)
        }
}