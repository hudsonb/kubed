package kubed.geo.projection

import kubed.geo.math.SQRT2
import kubed.math.EPSILON
import kubed.math.QUARTER_PI
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private const val k = 2.00276
private const val w = 1.11072


fun boggs() = boggs {}
fun boggs(init: MutableProjection.() -> Unit) = projection(BoggsProjector()) {
    scale = 160.857
    init()
}

class BoggsProjector : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val theta = mollweideBromleyTheta(PI, phi)
        return doubleArrayOf(k * lambda / (1 / cos(phi) + w / cos(theta)),
                            (phi + SQRT2 * sin(theta)) / k)
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val ky = k * y
        var theta = if(y < 0) -QUARTER_PI else QUARTER_PI

        var i = 25
        var phi: Double
        var delta: Double
        do {
            phi = ky - SQRT2 * sin(theta)
            delta = (sin(2 * theta) + 2 * theta - PI * sin(phi)) / (2 * cos(2 * theta) + 2 + PI * cos(phi) * SQRT2 * cos(theta))
            theta -= delta
        } while(abs(delta) > EPSILON && --i > 0)

        phi = ky - SQRT2 * sin(theta)

        return doubleArrayOf(x * (1 / cos(phi) + w / cos(theta)) / k, phi)
    }
}