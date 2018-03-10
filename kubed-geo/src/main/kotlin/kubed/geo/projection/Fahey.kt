package kubed.geo.projection

import kubed.math.toRadians
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.math.tan

fun fahey() = fahey {}
fun fahey(init: MutableProjection.() -> Unit) = projection(FaheyProjector()) {
    scale = 137.152
    init()
}

class FaheyProjector : InvertableProjector {
    private val k = cos(35.0.toRadians())

    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val t = tan(phi  /2)
        return doubleArrayOf(lambda * k * sqrt(1 - t * t),
                (1 + k) * t)
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val t = y / (1 + k)
        return doubleArrayOf(x / (k * sqrt(1 - t * t)),
                             2 * atan(t))
    }
}