package kubed.geo.projection

import kubed.geo.math.SQRT2
import kubed.math.asin
import kubed.math.EPSILON
import kubed.math.HALF_PI
import kotlin.math.*

fun mollweide() = mollweide {}
fun mollweide(init: MutableProjection.() -> Unit) = projection(MollweideProjector(SQRT2 / HALF_PI, SQRT2, PI)).apply {
    scale = 169.529
    init()
}

fun mollweideBromleyTheta(cp: Double, phi: Double): Double {
    val cpsinPhi = cp * sin(phi)

    var phi = phi
    var delta: Double
    var i = 30
    do {
        delta = (phi + sin(phi) - cpsinPhi) / (1 + cos(phi))
        phi -= delta
    } while(abs(delta) > EPSILON && --i > 0)

    return phi / 2
}

class MollweideProjector(private val cx: Double, private val cy: Double, private val cp: Double) : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val phi2 = mollweideBromleyTheta(cp, phi)
        return doubleArrayOf(cx * lambda * cos(phi2),
                             cy * sin(phi2))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val y2 = asin(y / cy)
        return doubleArrayOf(x / (cx * cos(y2)),
                             asin((2 * y2 + sin(2 * y2)) / cp))
    }
}

// TODO: Provide a mutable impl of MollweideBromely?