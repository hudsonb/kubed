package kubed.geo.projection

import kotlin.math.*

fun eckert1() = eckert1 {}
fun eckert1(init: MutableProjection.() -> Unit) = projection(Eckert1Projector()) {
    scale = 165.664
    init()
}

fun eckert2() = eckert2 {}
fun eckert2(init: MutableProjection.() -> Unit) = projection(Eckert2Projector()) {
    scale = 165.664
    init()
}

class Eckert1Projector : InvertableProjector {
    private val alpha = sqrt(8 / (3 * PI))

    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        return doubleArrayOf(alpha * lambda * (1 - abs(phi) / PI),
                             alpha * phi)
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val phi = y / alpha
        return doubleArrayOf(x / (alpha * (1 - abs(phi) / PI)),
                             phi)
    }
}

class Eckert2Projector : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val alpha = sqrt(4 - 3 * sin(abs(phi)))
        return doubleArrayOf(2 / sqrt(6 * PI) * lambda * alpha,
                             sign(phi) * sqrt(2 * PI / 3) * (2 - alpha))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val alpha = 2 - abs(y) / sqrt(2 * PI / 3)
        return doubleArrayOf(x * sqrt(6 * PI) / (2 * alpha),
                             sign(y) * asin((4 - alpha * alpha) / 3))
    }
}