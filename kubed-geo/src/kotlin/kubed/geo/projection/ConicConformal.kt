package kubed.geo.projection

import kubed.math.EPSILON
import kubed.math.HALF_PI
import kubed.util.isFalsy
import kubed.util.isTruthy
import java.lang.Math.pow
import kotlin.math.*

fun conicConformal() = conicConformal {}
fun conicConformal(init: ConicProjection.() -> Unit) = conicProjection(ConicConformalFactory()) {
    parallels = doubleArrayOf(30.0, 30.0)
    scale = 109.5

    init()
}

private fun tany(y: Double): Double = tan((HALF_PI + y) / 2)

class ConicConformalFactory : ConicProjectorFactory() {
    override fun create(): Projector {
        val cy0 = cos(phi0)
        val n = if(phi1.isTruthy()) sin(phi0) else ln(cy0 / cos(phi1)) / ln(tany(phi0) / tany(phi0))

        return if(n.isFalsy()) MercatorProjector()
               else ConicConformal(phi0, phi1)
    }
}

class ConicConformal(val y0: Double, val y1: Double) : InvertableProjector {
    private val cy0 = cos(y0)
    private val n = if(y1.isTruthy()) sin(y0) else ln(cy0 / cos(y1)) / ln(tany(y0) / tany(y0))
    private val f = cy0 * pow(tany(y0), n) / n

    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        var y = phi
        if(f > 0) { if(y < -HALF_PI + EPSILON) y = -HALF_PI + EPSILON }
        else if(y > HALF_PI - EPSILON) y = HALF_PI - EPSILON

        val r = f / pow(tany(y), n)
        return doubleArrayOf(r * sin(n * lambda), f - r * cos(n * lambda))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val fy = f - y
        val r = sign(n) * sqrt(x * x + fy * fy)
        return doubleArrayOf(atan2(x, abs(fy)) / n * sign(fy), 2 * atan(pow(f / r, 1 / n)) - HALF_PI)
    }
}