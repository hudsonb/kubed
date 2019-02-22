package kubed.geo.projection

import kubed.math.EPSILON
import kubed.math.EPSILON2
import kubed.math.asin
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun equalEarth() = equalEarth {}
fun equalEarth(init: MutableProjection.() -> Unit) = projection(EqualEarthProjector()) {
    scale = 177.158
    init()
}

private const val A1 = 1.340264
private const val A2 = -0.081106
private const val A3 = 0.000893
private const val A4 = 0.003796
private val M = sqrt(3.0) / 2

/**
 * The Equal Earth projection, by Bojan Šavrič et al., 2018.
 */
class EqualEarthProjector : InvertableProjector {
    private val iterations = 12

    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val l = asin(M * sin(phi))
        val l2 = l * l
        val l6 = l2 * l2 * l2
        return doubleArrayOf(lambda * cos(l) / (M * (A1 + 3 * A2 * l2 + l6 * (7 * A3 + 9 * A4 * l2))),
                             l * (A1 + A2 * l2 + l6 * (A3 + A4 * l2)))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        var l = y
        var l2 = l * l
        var l6 = l2 * l2 * l2

        for(i in 0 until iterations) {
            val fy = l * (A1 + A2 * l2 + l6 * (A3 + A4 * l2)) - y
            val fpy = A1 + 3 * A2 * l2 + l6 * (7 * A3 + 9 * A4 * l2)
            val delta = fy / fpy
            l -= delta
            l2 = l * l
            l6 = l2 * l2 * l2
            if(abs(delta) < EPSILON2) break
        }

        return doubleArrayOf(M * x * (A1 + 3 * A2 * l2 + l6 * (7 * A3 + 9 * A4 * l2)) / cos(l),
                             asin(sin(l) / M))
    }

}