package kubed.geo.projection

import kubed.geo.math.sqrt
import kubed.math.asin
import kubed.util.isTruthy
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun azimuthalInvert(angle: (Double) -> Double) = { x: Double, y: Double ->
    val z = sqrt(x * x + y * y)
    val c = angle(z)
    val sc = sin(c)
    val cc = cos(c)
    doubleArrayOf(atan2(x * sc, z * cc), asin(if (z == 0.0) z else y * sc / z))
}

abstract class Azimuthal(val scale: (Double) -> Double, val angle: (Double) -> Double) : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val cx = cos(lambda)
        val cy = cos(phi)
        val k = scale(cx * cy)

        return doubleArrayOf(k * cy * sin(lambda),
                k * sin(phi))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val z = sqrt(x * x + y * y)
        val c = angle(z)
        val sc = sin(c)
        val cc = cos(c)
        return doubleArrayOf(atan2(x * sc, z * cc),
                             asin(if(z.isTruthy()) y * sc / z else z))
    }
}