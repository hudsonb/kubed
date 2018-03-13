package kubed.geo.projection

import kubed.geo.Position
import kubed.util.isTruthy
import kotlin.math.*

fun bonne() = bonne {}
fun bonne(init: Parallel1Projection.() -> Unit) = Parallel1Projection(::bonneRaw).apply {
    scale = 123.082
    center = Position(0.0, 26.1441)
    parallel = 45.0

    init()
}

fun bonneRaw(phi0: Double) = when(phi0) {
    -0.0, +0.0 -> SinusoidalProjector()
    else -> BonneProjector(phi0)
}

class BonneProjector(private val phi0: Double) : InvertableProjector {
    private val cotPhi0 = 1.0 / tan(phi0)

    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val rho = cotPhi0 + phi0 - phi
        val e = if(rho.isTruthy()) lambda * cos(phi) / rho else rho
        return doubleArrayOf(rho * sin(e),
                             cotPhi0 - rho * cos(e))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val y2 = cotPhi0 - y
        val rho = sqrt(x * x + y2 * y2)
        val phi = cotPhi0 + phi0 - rho

        return doubleArrayOf(rho / cos(phi) * atan2(x, y2),
                             phi)
    }
}

