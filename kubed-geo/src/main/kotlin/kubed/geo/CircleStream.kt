package kubed.geo

import kubed.math.EPSILON
import kubed.math.TAU
import kubed.geo.math.acos
import kubed.util.isFalsy
import kotlin.math.cos
import kotlin.math.sin

/**
 * Generates a circle centered at [0°, 0°], with a given radius and precision.
 */
fun circleStream(stream: GeometryStream, radius: Double, delta: Double, direction: Int, t0: DoubleArray?, t1: DoubleArray?) {
    if(delta.isFalsy()) return

    val cosRadius = cos(radius)

    val step = direction * delta

    var ta: Double
    val tb: Double
    if(t0 == null) {
        ta = radius + direction * TAU
        tb = radius - step / 2
    }
    else {
        ta = circleRadius(cosRadius, t0)
        tb = circleRadius(cosRadius, t1!!)
        val b = if(direction > 0) ta < tb else ta > tb
        if(b) ta += direction * TAU
    }

    circleStream(stream, radius, delta, direction, ta, tb)
}

/**
 * Generates a circle centered at [0°, 0°], with a given radius and precision.
 */
private fun circleStream(stream: GeometryStream, radius: Double, delta: Double, direction: Int, t0: Double, t1: Double) {
    if(delta.isFalsy()) return

    val cosRadius = cos(radius)
    val sinRadius = sin(radius)
    val step = direction * delta

    var point: DoubleArray
    var t = t0
    while(if(direction > 0) t > t1 else t < t1) {
        point = spherical(doubleArrayOf(cosRadius, -sinRadius * cos(t), -sinRadius * sin(t)))
        stream.point(point[0], point[1], 0.0)
        t -= step
    }
}

/**
 * Returns the signed angle of a cartesian point relative to [cosRadius, 0, 0].
 */
private fun circleRadius(cosRadius: Double, point: DoubleArray): Double {
    val cp = cartesian(point)
    cp[0] -= cosRadius
    cartesianNormalizeInPlace(cp)
    val radius = acos(-cp[1])
    return ((if(-cp[2] < 0) -radius else radius) + TAU - EPSILON) % TAU
}