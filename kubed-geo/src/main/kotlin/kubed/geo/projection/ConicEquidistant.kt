package kubed.geo.projection

import kubed.math.EPSILON
import kotlin.math.*

fun conicEquidistant() = conicEquidistant {}
fun conicEquidistant(init: ConicProjection.() -> Unit) = conicProjection(ConicEquidistantProjectorFactory()) {
    scale = 131.154
    center = doubleArrayOf(0.0, 13.9389)
    init()
}

class ConicEquidistantProjectorFactory : ConicProjectorFactory() {
    var g: Double = Double.NaN

    override fun create(): Projector {
        val cy0 = cos(phi0)
        val n = when {
            phi0 == phi1 -> sin(phi0)
            else -> (cy0 - cos(phi1)) / (phi1 - phi0)
        }

        g = cy0 / n + phi0

        return when {
            abs(n) < EPSILON -> EquirectangularProjector()
            else -> object : InvertableProjector {
                override fun invoke(lambda: Double, phi: Double): DoubleArray {
                    val gy = g - phi
                    val nx = n * lambda
                    return doubleArrayOf(gy * sin(nx), g - gy * cos(nx))
                }

                override fun invert(x: Double, y: Double): DoubleArray {
                    val gy = g - y
                    return doubleArrayOf(atan2(x, abs(gy)) / n * sign(gy), g - sign(n) * sqrt(x * x + gy * gy))
                }
            }
        }
    }
}

