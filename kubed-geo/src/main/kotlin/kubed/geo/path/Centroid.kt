package kubed.geo.path

import kubed.geo.MutableGeometryStream
import kubed.geo.math.sqrt
import kubed.util.isTruthy

class Centroid : MutableGeometryStream() {
    private var X0 = 0.0
    private var Y0 = 0.0
    private var Z0 = 0.0
    private var X1 = 0.0
    private var Y1 = 0.0
    private var Z1 = 0.0
    private var X2 = 0.0
    private var Y2 = 0.0
    private var Z2 = 0.0
    private var x00 = Double.NaN
    private var y00 = Double.NaN
    private var x0 = Double.NaN
    private var y0 = Double.NaN

    fun result(): DoubleArray {
        val centroid = when {
            Z2.isTruthy() -> doubleArrayOf(X2 / Z2, Y2 / Z2)
            Z1.isTruthy() -> doubleArrayOf(X1 / Z1, Y1 / Z1)
            Z0.isTruthy() -> doubleArrayOf(X0 / Z0, Y0 / Z0)
            else -> doubleArrayOf(Double.NaN, Double.NaN)
        }

        X0 = 0.0
        Y0 = 0.0
        Z0 = 0.0
        X1 = 0.0
        Y1 = 0.0
        Z1 = 0.0
        X2 = 0.0
        Y2 = 0.0
        Z2 = 0.0

        return centroid
    }

    private fun centroidPoint(x: Double, y: Double, @Suppress("UNUSED_PARAMETER") z: Double) {
        X0 += x
        Y0 += y
        ++Z0
    }

    @Suppress("unused")
    private fun centroidLineStart() {
        point = ::centroidPointFirstLine
    }

    private fun centroidPointFirstLine(x: Double, y: Double, z: Double) {
        point = ::centroidPointLine
        x0 = x
        y0 = y
        centroidPoint(x, y, z)
    }

    private fun centroidPointLine(x: Double, y: Double, @Suppress("UNUSED_PARAMETER") z: Double) {
        val dx = x - x0
        val dy = y - y0
        val z = sqrt(dx * dx + dy * dy)
        X1 += z * (x0 + x) / 2;
        Y1 += z * (y0 + y) / 2;
        Z1 += z
        centroidPoint(x, y, 0.0)
    }

    @Suppress("unused")
    private fun centroidLineEnd() {
        point = ::centroidPoint
    }

    @Suppress("unused")
    private fun centroidRingStart() {
        point = ::centroidPointFirstRing
    }

    @Suppress("unused")
    private fun centroidRingEnd() = centroidPointRing(x00, y00, 0.0)

    private fun centroidPointFirstRing(x: Double, y: Double, z: Double) {
        point = ::centroidPointRing
        x00 = x
        y00 = y
        x0 = x
        y0 = y
        centroidPoint(x, y, 0.0)
    }

    private fun centroidPointRing(x: Double, y: Double, z: Double) {
        val dx = x - x0
        val dy = y - y0
        var z = sqrt(dx * dx + dy * dy)

        X1 += z * (x0 + x) / 2
        Y1 += z * (y0 + y) / 2
        Z1 += z

        z = y0 * x - x0 * y
        X2 += z * (x0 + x)
        Y2 += z * (y0 + y)
        Z2 += z * 3
        x0 = x
        y0 = y
        centroidPoint(x, y, 0.0)
    }
}