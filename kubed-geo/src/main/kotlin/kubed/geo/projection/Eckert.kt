package kubed.geo.projection

import kubed.math.EPSILON
import kubed.math.HALF_PI
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

fun eckert3() = eckert3 {}
fun eckert3(init: MutableProjection.() -> Unit) = projection(Eckert3Projector()) {
    scale = 180.739
    init()
}

fun eckert4() = eckert4 {}
fun eckert4(init: MutableProjection.() -> Unit) = projection(Eckert4Projector()) {
    scale = 180.739
    init()
}

fun eckert5() = eckert4 {}
fun eckert5(init: MutableProjection.() -> Unit) = projection(Eckert5Projector()) {
    scale = 173.044
    init()
}

fun eckert6() = eckert4 {}
fun eckert6(init: MutableProjection.() -> Unit) = projection(Eckert6Projector()) {
    scale = 173.044
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

class Eckert3Projector : InvertableProjector {
    private val k = sqrt(PI * (4 + PI))
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        return doubleArrayOf(2 / k * lambda * (1 + sqrt(1 - 4 * phi * phi / (PI * PI))),
                             4 / k * phi)
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val k2 = k / 2
        return doubleArrayOf(x * k2 / (1 + sqrt(1 - y * y * (4 + PI) / (4 * PI))),
                             y * k / 2)
    }
}

class Eckert4Projector : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val k = (2 + HALF_PI) * sin(phi)
        var phi2 = phi / 2
        var i = 0
        var delta = Double.POSITIVE_INFINITY
        while(i < 10 && abs(delta) > EPSILON) {
            val cosPhi = cos(phi)
            delta = (phi + sin(phi) * (cosPhi + 2) - k) / (2 * cosPhi * (1 + cosPhi))
            phi2 -= delta
            i++
        }

        return doubleArrayOf(2 / sqrt(PI * (4 + PI)) * lambda * (1 + cos(phi)),
                             2 * sqrt(PI / (4 + PI)) * sin(phi))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val a = y * sqrt((4 + PI) / PI) / 2
        val k = asin(a)
        val c = cos(k)

        return doubleArrayOf(x / (2 / sqrt(PI * (4 + PI)) * (1 + c)),
                             asin((k + a * (c + 2)) / (2 + HALF_PI)))
    }
}

class Eckert5Projector : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        return doubleArrayOf(lambda * (1 + cos(phi)) / sqrt(2 + PI),
                             2 * phi / sqrt(2 + PI))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val k = sqrt(2 + PI)
        val phi = y * k / 2
        return doubleArrayOf(k * x / (1 + cos(phi)),
                             phi)
    }
}

class Eckert6Projector : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        var k = (1 + HALF_PI) * sin(phi)
        var i = 0
        var phi = phi
        var delta = Double.POSITIVE_INFINITY
        while(i < 10 && abs(delta) > EPSILON) {
            delta = (phi + sin(phi) - k) / (1 + cos(phi))
            phi -= delta
            i++
        }
        k = sqrt(2 + PI)

        return doubleArrayOf(lambda * (1 + cos(phi)) / k,
                             2 * phi / k)
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val j = 1 + HALF_PI
        val k = sqrt(j / 2)

        val yk = y * k
        return doubleArrayOf(x * 2 * k / (1 + cos(yk)),
                             asin((yk + sin(yk)) / j))
    }
}