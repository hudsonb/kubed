import kubed.geo.*
import kubed.geo.math.Area
import kubed.math.EPSILON
import kubed.math.toDegrees
import kubed.math.toRadians
import java.math.BigDecimal
import kotlin.math.abs

class Bounds : MutableGeometryStream() {
    private var lambda0 = Double.NaN
    private var phi0 = Double.NaN
    private var lambda1 = Double.NaN
    private var phi1 = Double.NaN // bounds
    private var lambda2 = Double.NaN // previous lambda-coordinate
    private var lambda00 = Double.NaN
    private var phi00 = Double.NaN // first point
    private var p0: DoubleArray? = null // previous 3D point
    private var deltaSum = BigDecimal.ZERO
    private lateinit var ranges: ArrayList<DoubleArray>
    private lateinit var range: DoubleArray

    private var areaStream = Area()

    init {
        point = ::boundsPoint
        lineStart = ::boundsLineStart
        lineEnd = ::boundsLineEnd
        polygonStart = {
            point = ::boundsRingPoint
            lineStart = ::boundsRingStart
            lineEnd = ::boundsRingEnd
            deltaSum = BigDecimal.ZERO
            areaStream.polygonStart()
        }
        polygonEnd = {
            areaStream.polygonEnd()
            point = ::boundsPoint
            lineStart = ::boundsLineStart
            lineEnd = ::boundsLineEnd
            if(areaStream.areaRingSum.toDouble() < 0) {
                lambda1 = 180.0
                lambda0 = -lambda1
                phi1 = 90.0
                phi0 = -phi1
            }
            else {
                val sum = deltaSum.toDouble()
                if(sum > EPSILON) phi1 = 90.0
                else if(sum < -EPSILON) phi0 = -90.0
            }
            range[0] = lambda0
            range[1] = lambda1
        }
    }

    private fun boundsPoint(lambda: Double, phi: Double, z: Double) {
        lambda0 = lambda
        lambda1 = lambda

        range = doubleArrayOf(lambda, lambda)
        ranges.add(range)
        if(phi < phi0) phi0 = phi
        if(phi > phi1) phi1 = phi
    }

    private fun linePoint(lambda: Double, phi: Double, z: Double) {
        val p = cartesian(doubleArrayOf(lambda.toRadians(), phi.toRadians()))
        if(p0 != null) {
            val normal = cartesianCross(p0!!, p)
            val equatorial = doubleArrayOf(normal[1], -normal[0], 0.0)
            var inflection = cartesianCross(equatorial, normal)
            cartesianNormalizeInPlace(inflection)
            inflection = spherical(inflection)
            val delta = lambda - lambda2
            val sign = if(delta > 0) 1 else -1
            var lambdai = inflection[0].toDegrees() * sign
            val antimeridian = abs(delta) > 180
            val phii: Double
            if(antimeridian xor (sign * lambda2 < lambdai && lambdai < sign * lambda)) {
                phii = inflection[1].toDegrees()
                if(phii > phi1) phi1 = phii
            }
            else {
                lambdai = (lambdai + 360) % 360-180
                if(antimeridian xor (sign * lambda2 < lambdai && lambdai < sign * lambda)) {
                    phii = -inflection[1].toDegrees()
                    if(phii < phi0) phi0 = phii
                }
                else {
                    if(phi < phi0) phi0 = phi
                    if(phi > phi1) phi1 = phi
                }
            }

            if(antimeridian) {
                if(lambda < lambda2) {
                    if(angle(lambda0, lambda) > angle(lambda0, lambda1)) lambda1 = lambda
                }
                else if(angle(lambda, lambda1) > angle(lambda0, lambda1)) lambda0 = lambda
            }
            else {
                if(lambda1 >= lambda0) {
                    if(lambda < lambda0) lambda0 = lambda
                    if(lambda > lambda1) lambda1 = lambda
                }
                else {
                    if(lambda > lambda2) {
                        if(angle(lambda0, lambda) > angle(lambda0, lambda1)) lambda1 = lambda
                    }
                    else if(angle(lambda, lambda1) > angle(lambda0, lambda1)) lambda0 = lambda;
                }
            }
        }
        else {
            lambda0 = lambda
            lambda1 = lambda
            range = doubleArrayOf(lambda, lambda)
            ranges.add(range);
        }

        if(phi < phi0) phi0 = phi
        if(phi > phi1) phi1 = phi
        p0 = p
        lambda2 = lambda
    }

    private fun boundsLineStart() {
        point = ::linePoint
    }

    private fun boundsLineEnd() {
        range[0] = lambda0
        range[1] = lambda1
        point = ::boundsPoint
        p0 = null
    }

    private fun boundsRingPoint(lambda: Double, phi: Double, z: Double)
    {
        if(p0 != null) {
            val delta = lambda - lambda2
            deltaSum.add(if(abs(delta) > 180) (delta + (if(delta > 0) 360  else -360)).toBigDecimal() else delta.toBigDecimal())
        }
        else {
            lambda00 = lambda
            phi00 = phi
        }
        areaStream.point(lambda, phi, 0.0)
        linePoint(lambda, phi, 0.0)
    }

    private fun boundsRingStart() = areaStream.lineStart()

    private fun boundsRingEnd() {
        boundsRingPoint(lambda00, phi00, 0.0)
        areaStream.lineEnd()
        if(abs(deltaSum.toDouble()) > EPSILON) {
            lambda0 = -180.0
            lambda1 = 180.0
        }
        range[0] = lambda0
        range[1] = lambda1
        p0 = null
    }

    // Finds the left-right distance between two longitudes.
    // This is almost the same as (lambda1 - lambda0 + 360°) % 360°, except that we want
    // the distance between ±180° to be 360°.
    private fun angle(lambda0: Double, lambda1: Double): Double
    {
        val delta = lambda1 - lambda0
        return if(delta < 0) lambda1 + 360 else lambda1
    }

    private fun rangeCompare(a: DoubleArray, b: DoubleArray) = a[0] - b[0]


    private fun rangeContains(range: DoubleArray, x: Double): Boolean {
        return if(range[0] <= range[1]) range[0] <= x && x <= range[1]
        else x < range[0] || range[1] < x
    }
}
