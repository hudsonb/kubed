package kubed.geo.projection

import kubed.math.TAU
import kotlin.math.PI
import kotlin.math.sqrt

fun kavrayskiy7() = kavrayskiy7 {}
fun kavrayskiy7(init: MutableProjection.() -> Unit) = projection(Kavrayskiy7Projector()) {
    scale = 135.264
    init()
}

class Kavrayskiy7Projector : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        return doubleArrayOf(3 / TAU * lambda * sqrt(PI * PI / 3 - phi * phi),
                             phi)
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        return doubleArrayOf(TAU / 3 * x / sqrt(PI * PI / 3 - y * y),
                             y)
    }
}