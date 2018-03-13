package kubed.geo.clip

import kubed.geo.*
import kubed.geo.math.sqrt
import kubed.math.EPSILON
import kubed.math.toRadians
import kubed.util.isFalsy
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

fun clipCircle(radius: Double) = { stream: GeometryStream -> ClipStream(CircleClip(radius), stream) }

class CircleClip(val radius: Double) : Clip {
    private val cr = cos(radius)
    private val delta = 6.0.toRadians()
    private val smallRadius = cr > 0
    private val notHemisphere = abs(cr) > EPSILON

    override val start: DoubleArray
        get() = if(smallRadius) doubleArrayOf(0.0, -radius) else doubleArrayOf(-PI, radius - PI)

    override fun isVisible(x: Double, y: Double) = cos(x) * cos(y) > cr

    override fun clipLine(stream: GeometryStream): IntersectStream {
        var point0: DoubleArray? = null
        var c0 = 0
        var v0 = false
        var v00 = false

       return object : IntersectStream {
           private var _clean = 0

           override var clean: Int = _clean
                get() = _clean or ((if(v00 && v0) 1 else 0) shl 1)

           override fun lineStart() {
               v00 = false
               v0 = false
               _clean = 1
           }

           override fun point(x: Double, y: Double, z: Double) {
               val point1 = doubleArrayOf(x, y)
               var point2: DoubleArray?
               var v = isVisible(x, y)
               val c = if(smallRadius) {
                   if(v) 0 else code(x, y)
               }
               else if(v) code(x + if (x < 0) PI else -PI, y)
               else 0

               if(point0 == null) {
                   v0 = v
                   v00 = v
                   if(v) stream.lineStart()
               }

               if(v != v0) {
                   point2 = intersect(point0!!, point1)
                   if(point2 == null || pointsEqual(point0!!, point2) || pointsEqual(point1, point2)) {
                       point1[0] += EPSILON
                       point1[1] += EPSILON
                       v = isVisible(point1[0], point1[1])
                   }
               }

               if(v != v0) {
                   _clean = 0
                   if(v) {
                       // outside going in
                       stream.lineStart()
                       point2 = intersect(point1, point0!!)
                       stream.point(point2!![0], point2[1], 0.0)
                   }
                   else {
                       // inside going out
                       point2 = intersect(point0!!, point1)
                       stream.point(point2!![0], point2[1], 0.0)
                       stream.lineEnd()
                   }
                   point0 = point2
               }
               else if(notHemisphere && point0 != null && (smallRadius xor v)) {
                   // If the codes for two points are different, or are both zero,
                   // and this segment intersects with the small circle.
                   if((c and c0) == 0) {
                       val t = intersect2(point1, point0!!)
                       if(t != null) {
                           _clean = 0
                           if(smallRadius) {
                               stream.lineStart()
                               stream.point(t[0][0], t[0][1], 0.0)
                               stream.point(t[1][0], t[1][1], 0.0)
                               stream.lineEnd()
                           }
                           else {
                               stream.point(t[1][0], t[1][1], 0.0)
                               stream.lineEnd()
                               stream.lineStart()
                               stream.point(t[0][0], t[0][1], 0.0)
                           }
                       }
                   }
               }
               if(v && (point0 == null || !pointsEqual(point0!!, point1))) {
                   stream.point(point1[0], point1[1], 0.0)
               }
               point0 = point1
               v0 = v
               c0 = c
           }

           override fun lineEnd() {
               if(v0) stream.lineEnd()
               point0 = null
           }
       }
    }

    override fun interpolate(from: DoubleArray?, to: DoubleArray?, direction: Int, stream: GeometryStream) =
            circleStream(stream, radius, delta, direction, from, to)

    private fun code(lambda: Double, phi: Double): Int {
        val r = if (smallRadius) radius else PI - radius
        var code = 0

        if (lambda < -r) code = code or 1  // Left
        else if (lambda > r) code = code or 2 // Right
        if (phi < -r) code = code or 4 // Below
        else if (phi > r) code or 8 // Above

        return code
    }

    /**
     * Intersects the great circle between a and b with the clip circle.
     */
    fun intersect(a: DoubleArray, b: DoubleArray): DoubleArray? {
        val pa = cartesian(a)
        val pb = cartesian(b)

        // We have two planes, n1.p = d1 and n2.p = d2.
        // Find intersection line p(t) = c1 n1 + c2 n2 + t (n1 ⨯ n2).
        val n1 = doubleArrayOf(1.0, 0.0, 0.0) // normal
        val n2 = cartesianCross(pa, pb)
        val n2n2 = cartesianDot(n2, n2)
        val n1n2 = n2[0] // cartesianDot(n1, n2),
        val determinant = n2n2 - n1n2 * n1n2

        // Two polar points.
        if (determinant.isFalsy()) return a

        val c1 = cr * n2n2 / determinant
        val c2 = -cr * n1n2 / determinant
        val n1xn2 = cartesianCross(n1, n2)
        val A = cartesianScale(n1, c1)
        val B = cartesianScale(n2, c2)
        cartesianAddInPlace(A, B)

        // Solve |p(t)|^2 = 1.
        val u = n1xn2
        val w = cartesianDot(A, u)
        val uu = cartesianDot(u, u)
        val t2 = w * w - uu * (cartesianDot(A, A) - 1)

        if(t2 < 0) return null

        val t = sqrt(t2)
        var q = cartesianScale(u, (-w - t) / uu)
        cartesianAddInPlace(q, A)
        q = spherical(q)

        return q
    }

    /**
     * Intersects the great circle between a and b with the clip circle.
     */
    fun intersect2(a: DoubleArray, b: DoubleArray): Array<DoubleArray>? {
        val pa = cartesian(a)
        val pb = cartesian(b)

        // We have two planes, n1.p = d1 and n2.p = d2.
        // Find intersection line p(t) = c1 n1 + c2 n2 + t (n1 ⨯ n2).
        val n1 = doubleArrayOf(1.0, 0.0, 0.0) // normal
        val n2 = cartesianCross(pa, pb)
        val n2n2 = cartesianDot(n2, n2)
        val n1n2 = n2[0] // cartesianDot(n1, n2),
        val determinant = n2n2 - n1n2 * n1n2

        // Two polar points.
        if(determinant.isFalsy()) return null

        val c1 = cr * n2n2 / determinant
        val c2 = -cr * n1n2 / determinant
        val n1xn2 = cartesianCross(n1, n2)
        val A = cartesianScale(n1, c1)
        val B = cartesianScale(n2, c2)
        cartesianAddInPlace(A, B)

        // Solve |p(t)|^2 = 1.
        val u = n1xn2
        val w = cartesianDot(A, u)
        val uu = cartesianDot(u, u)
        val t2 = w * w - uu * (cartesianDot(A, A) - 1)

        if(t2 < 0) return null

        val t = sqrt(t2)
        var q = cartesianScale(u, (-w - t) / uu)
        cartesianAddInPlace(q, A)
        q = spherical(q)

        // Two intersection points.
        var lambda0 = a[0]
        var lambda1 = b[0]
        var phi0 = a[1]
        var phi1 = b[1]
        var z: Double

        if (lambda1 < lambda0) {
            z = lambda0
            lambda0 = lambda1
            lambda1 = z
        }

        val delta = lambda1 - lambda0
        val polar = abs(delta - PI) < EPSILON
        val meridian = polar || delta < EPSILON

        if (!polar && phi1 < phi0) {
            z = phi0
            phi0 = phi1
            phi1 = z
        }

        var between = false
        if(meridian) {
            if(polar) {
                if((phi0 + phi1 > 0) xor (q[1] < if(abs(q[0] - lambda0) < EPSILON) phi0 else phi1)) between = true
                else if(q[1] in phi0..phi1) between = true
            }
        }
        else if((delta > PI) xor (q[0] in lambda0..lambda1)) between = true

        if(between) {
            val q1 = cartesianScale(u, (-w + t) / uu)
            cartesianAddInPlace(q1, A)
            return arrayOf(q, spherical(q1))
        }

        return null
    }
}