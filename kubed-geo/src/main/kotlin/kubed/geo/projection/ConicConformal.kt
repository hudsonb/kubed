package kubed.geo.projection

import kubed.geo.math.sqrt
import kubed.geo.math.tany
import kubed.math.EPSILON
import kubed.math.HALF_PI
import kubed.util.isFalsy
import java.lang.Math.cos
import java.lang.Math.pow
import kotlin.math.*

fun conicConformal() = conicConformal {}
fun conicConformal(init: ConicProjection.() -> Unit) = ConicProjection(::conicConformalRaw).apply {
    scale = 109.5
    parallels = doubleArrayOf(30.0, 30.0)

    init()
}

fun conicConformalRaw(y0: Double, y1: Double): Projector {
    val cy0 = cos(y0)
    val n = if(y0 == y1) sin(y0) else ln(cy0 / cos(y1)) / ln(tany(y1) / tany(y0))

    return when {
        n.isFalsy() -> MercatorProjector()
        else -> {
            val f = cy0 * pow(tany(y0), n) / n
            object : InvertableProjector {
                override fun invoke(lambda: Double, phi: Double): DoubleArray {
                    var y = phi
                    if(f > 0) {
                        if(y < -HALF_PI + EPSILON) y = -HALF_PI + EPSILON
                    }
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
        }
    }
}