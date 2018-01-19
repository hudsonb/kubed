package kubed.geo.math

import kubed.geo.cartesian
import kubed.geo.cartesianCross
import kubed.geo.cartesianNormalizeInPlace
import kubed.math.EPSILON
import kubed.math.QUARTER_PI
import kubed.math.TAU
import kubed.math.asin
import kubed.util.isTruthy
import java.math.BigDecimal
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun polygonContains(polygon: List<List<DoubleArray>>, point: DoubleArray): Boolean {
    val lambda = point[0]
    val phi = point[1]
    val normal = doubleArrayOf(sin(lambda), -cos(lambda), 0.0)
    var angle = 0.0
    var winding = 0

    var sum = BigDecimal.ZERO

    for(i in polygon.indices) {
        val ring = polygon[i]
        if(ring.isEmpty()) continue

        val point0 = ring.last()
        val lambda0 = point0[0]
        val phi0 = point0[1] / 2 +  QUARTER_PI
        val sinPhi0 = sin(phi0)
        val cosPhi0 = cos(phi0)

        for(j in ring.indices) {
            val point1 = ring[j]
            val lambda1 = point1[0]
            val phi1 = point1[1] / 2 + QUARTER_PI
            val sinPhi1 = sin(phi1)
            val cosPhi1 = cos(phi1)
            val delta = lambda1 - lambda0
            val sign = if(delta >= 0) 1 else -1
            val absDelta = sign * delta
            val antimeridian = absDelta > PI
            val k = sinPhi0 * sinPhi1

            sum = sum.add(atan2(k * sign * sin(absDelta), cosPhi0 * cosPhi1 + k * cos(absDelta)).toBigDecimal())
            angle += if(antimeridian) delta + sign * TAU else delta

            // Are the longitudes either side of the point’s meridian (lambda),
            // and are the latitudes smaller than the parallel (phi)?
            if(antimeridian xor (lambda0 >= lambda) xor (lambda1 >= lambda)) {
                val arc = cartesianCross(cartesian(point0), cartesian(point1))
                cartesianNormalizeInPlace(arc)
                val intersection = cartesianCross(normal, arc)
                cartesianNormalizeInPlace(intersection)
                val phiArc = (if(antimeridian xor (delta >= 0)) 1 else -1) * asin(intersection[2])
                if(phi > phiArc || phi == phiArc && (arc[0].isTruthy() || arc[1].isTruthy())) {
                    winding += if(antimeridian xor (delta >= 0)) 1 else -1
                }
            }
        }
    }

    // First, determine whether the South pole is inside or outside:
    //
    // It is inside if:
    // * the polygon winds around it in a clockwise direction.
    // * the polygon does not (cumulatively) wind around it, but has a negative
    //   (counter-clockwise) area.
    //
    // Second, count the (signed) number of times a segment crosses a lambda
    // from the point to the South pole.  If it is zero, then the point is the
    // same side as the South pole.

    return (angle < -EPSILON || angle < EPSILON && sum.toDouble() < -EPSILON) xor ((winding and 1) != 0)
}