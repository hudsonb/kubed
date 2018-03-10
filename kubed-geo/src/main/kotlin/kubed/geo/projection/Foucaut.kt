package kubed.geo.projection

import kubed.geo.math.SQRT_PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.tan

fun foucaut() = foucaut {}
fun foucaut(init: MutableProjection.() -> Unit) = projection(FoucautProjector()) {
    scale = 135.264
    init()
}

class FoucautProjector : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val k = phi / 2
        val cosk = cos(k)
        return doubleArrayOf(2 * lambda / SQRT_PI * cos(phi) * cosk * cosk,
                             SQRT_PI * tan(k))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val k = atan(y / SQRT_PI)
        val cosk = cos(k)
        val phi = 2 * k
        return doubleArrayOf(x * SQRT_PI / 2 / (cos(phi) * cosk * cosk),
                             phi)
    }

}