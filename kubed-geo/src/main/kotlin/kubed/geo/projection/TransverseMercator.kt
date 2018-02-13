package kubed.geo.projection

import kubed.math.HALF_PI
import kubed.math.toRadians
import kotlin.math.*

fun transverseMercator() = transverseMercator {}
fun transverseMercator(init: TransverseMercatorProjection.() -> Unit) = transverseMercatorProjection {
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
    override fun recenter() {
        val lambda = (-center.latitude % 360).toRadians()
        val phi = (center.longitude % 360).toRadians()
        val deltaLambda = (rotateX % 360).toRadians()
        val deltaPhi = (rotateY % 360).toRadians()
        val deltaGamma = ((rotateZ + 90) % 360).toRadians()
        recenter(lambda, phi, deltaLambda, deltaPhi, deltaGamma)
    }
}