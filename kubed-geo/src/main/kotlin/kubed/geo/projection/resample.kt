package kubed.geo.projection

import kubed.geo.FilterGeometryStream
import kubed.geo.GeometryStream
import kubed.geo.MutableGeometryStream
import kubed.geo.cartesian
import kubed.math.EPSILON
import kubed.math.asin
import kubed.geo.math.sqrt
import kubed.math.toRadians
import kubed.util.isTruthy
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos

const val MAX_DEPTH = 16
val COS_MIN_DISTANCE = cos(30.0.toRadians())

fun resample(project: Transform, delta2: Double) = if(delta2.isTruthy()) _resample(project, delta2) else resampleNone(project)

private fun resampleNone(project: Projector): Transformer {
    return { stream: GeometryStream ->
        object : FilterGeometryStream(stream) {
            override fun point(x: Double, y: Double, z: Double) {
                val p = project(x, y)
                stream.point(p[0], p[1], 0.0)
            }
        }
    }
}

private fun _resample(project: Projector, delta2: Double): Transformer {
    fun resampleLineTo(x0: Double, y0: Double, lambda0: Double, a0: Double, b0: Double, c0: Double,
                               x1: Double, y1: Double, lambda1: Double, a1: Double, b1: Double, c1: Double,
                               depth: Int, stream: GeometryStream) {
        var depth = depth

        val dx = x1 - x0
        val dy = y1 - y0
        val d2 = dx * dx + dy * dy
        if(d2 > 4 * delta2 && depth.isTruthy()) {
            depth--
            var a = a0 + a1
            var b = b0 + b1
            var c = c0 + c1
            val m = sqrt(a * a + b * b + c * c)
            c /= m
            val phi2 = asin(c)
            val lambda2 = when {
                abs(abs(c) - 1) < EPSILON || abs(lambda0 - lambda1) < EPSILON -> (lambda0 + lambda1) / 2
                else -> atan2(b, a)
            }
            val p = project(lambda2, phi2)
            val x2 = p[0]
            val y2 = p[1]
            val dx2 = x2 - x0
            val dy2 = y2 - y0
            val dz = dy * dx2 - dx * dy2
            if(dz * dz / d2 > delta2 // perpendicular projected distance
                    || abs((dx * dx2 + dy * dy2) / d2 - 0.5) > 0.3 // midpoint close to an end
                    || a0 * a1 + b0 * b1 + c0 * c1 < COS_MIN_DISTANCE) { // angular distance
                a /= m
                b /= m
                resampleLineTo(x0, y0, lambda0, a0, b0, c0, x2, y2, lambda2, a, b, c, depth, stream)
                stream.point(x2, y2, 0.0)
                resampleLineTo(x2, y2, lambda2, a, b, c, x1, y1, lambda1, a1, b1, c1, depth, stream)
            }
        }
    }

    return { stream: GeometryStream ->
        object : MutableGeometryStream() {
            // First point
            var lambda00 = Double.NaN
            var x00 = Double.NaN
            var y00 = Double.NaN
            var a00 = Double.NaN
            var b00 = Double.NaN
            var c00 = Double.NaN

            // Previous point
            var lambda0 = Double.NaN
            var x0 = Double.NaN
            var y0 = Double.NaN
            var a0 = Double.NaN
            var b0 = Double.NaN
            var c0 = Double.NaN

            init {
                point = ::defaultPoint
                lineStart = ::defaultLineStart
                lineEnd = ::defaultLineEnd
                polygonStart = { stream.polygonStart(); lineStart = ::ringStart }
                polygonEnd = { stream.polygonEnd(); lineStart = ::defaultLineStart }
            }

            private fun defaultPoint(x: Double, y: Double, @Suppress("UNUSED_PARAMETER") z: Double) {
                val p = project(x, y)
                stream.point(p[0], p[1], 0.0)
            }

            private fun defaultLineStart() {
                x0 = Double.NaN
                point = ::linePoint
                stream.lineStart()
            }

            private fun linePoint(lambda: Double, phi: Double, z: Double) {
                val c = cartesian(doubleArrayOf(lambda, phi))
                val p = project(lambda, phi)
                resampleLineTo(x0, y0, lambda0, a0, b0, c0, p[0], p[1], lambda, c[0], c[1], c[2], MAX_DEPTH, stream)
                x0 = p[0]
                y0 = p[1]
                lambda0 = lambda
                a0 = c[0]
                b0 = c[1]
                c0 = c[2]
                stream.point(x0, y0, z)
            }

            private fun defaultLineEnd() {
                point = ::defaultPoint
                stream.lineEnd()
            }

            private fun ringStart() {
                defaultLineStart()
                point = ::ringPoint
                lineEnd = ::ringEnd
            }

            private fun ringPoint(lambda: Double, phi: Double, z: Double) {
                lambda00 = lambda
                linePoint(lambda, phi, 0.0)
                x00 = x0
                y00 = y0
                a00 = a0
                b00 = b0
                c00 = c0
                point = ::linePoint
            }

            private fun ringEnd() {
                resampleLineTo(x0, y0, lambda0, a0, b0, c0, x00, y00, lambda00, a00, b00, c00, MAX_DEPTH, stream)
                lineEnd = ::defaultLineEnd
                lineEnd()
            }
        }
    }
}