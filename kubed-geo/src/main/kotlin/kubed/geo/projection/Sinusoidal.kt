package kubed.geo.projection

import kotlin.math.cos

fun sinusoidal() = sinusoidal {}
fun sinusoidal(init: MutableProjection.() -> Unit) = projection(SinusoidalProjector()) {
    scale = 151.63
    init()
}

class SinusoidalProjector : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        return doubleArrayOf(lambda * cos(phi),
                             phi)
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        return doubleArrayOf(x / cos(y),
                             y)
    }
}