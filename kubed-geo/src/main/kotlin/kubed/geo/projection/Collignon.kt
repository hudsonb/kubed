package kubed.geo.projection

import kubed.geo.Position
import kubed.geo.math.SQRT_PI
import kubed.math.asin
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

fun collignon() = collignon {}
fun collignon(init: MutableProjection.() -> Unit) = projection(CollignonProjector()) {
    scale = 95.6464
    center = Position(0.0, 30.0)
    init()
}

class CollignonProjector : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val alpha = sqrt(1 - sin(phi))
        return doubleArrayOf((2 / SQRT_PI) * lambda * alpha, SQRT_PI * (1 - alpha))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        var lambda = y / SQRT_PI - 1
        lambda *= lambda

        return doubleArrayOf(if(lambda > 0) x * sqrt(PI / lambda) / 2 else 0.0,
                             asin(1 - lambda))
    }
}