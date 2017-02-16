package kubed.shape.curve

import javafx.scene.Node
import javafx.scene.canvas.GraphicsContext
import kubed.path.Context
import kubed.path.PathContext
import kubed.util.isTruthy

abstract class AbstractMonotoneCurve(val context: Context) : Curve {
    var point = 0
    var x0 = Double.NaN
    var y0 = Double.NaN
    var x1 = Double.NaN
    var y1 = Double.NaN
    var t0 = Double.NaN
    var t1 = Double.NaN

    // According to https://en.wikipedia.org/wiki/Cubic_Hermite_spline#Representations
    // "you can express cubic Hermite interpolation in terms of cubic Bézier curves
    // with respect to the four values p0, p0 + m0 / 3, p1 - m1 / 3, p1".
    fun monotonePoint(t0: Double, t1: Double) {
        val dx = (x1 - x0) / 3;
        context.bezierCurveTo(x0 + dx, y0 + dx * t0, x1 - dx, y1 - dx * t1, x1, y1)
    }

    fun sign(x: Double): Double = when {
        x < 0 -> -1.0
        else -> 1.0
    }

    // Calculate the slopes of the tangents (Hermite-type interpolation) based on
    // the following paper: Steffen, M. 1990. A Simple Method for Monotonic
    // Interpolation in One Dimension. Astronomy and Astrophysics, Vol. 239, NO.
    // NOV(II), P. 443, 1990.
    fun slope3(x2: Double, y2: Double): Double {
        val h0 = x1 - x0
        val h1 = x2 - x1
        val d0 = if(h0.isTruthy()) h0 else if(h1 < 0) 0.0 else -0.0
        val d1 = if(h1.isTruthy()) h0 else if(h0 < 0) 0.0 else -0.0
        val s0 = when(d0) {
            0.0 -> Double.POSITIVE_INFINITY
            -0.0 -> Double.NEGATIVE_INFINITY
            else -> (y1 - y0) / d0
        }
        val s1 = when(d1) {
            0.0 -> Double.POSITIVE_INFINITY
            -0.0 -> Double.NEGATIVE_INFINITY
            else -> (y2 - y1) / d1
        }
        val p = (s0 * h1 + s1 * h0) / (h0 + h1)
        val m = Math.min(Math.min(Math.abs(s0), Math.abs(s1)), 0.5 * Math.abs(p))
        val result = (sign(s0) + sign(s1)) * m
        return if(result.isTruthy()) result else 0.0
    }

    // Calculate a one-sided slope
    fun slope2(t: Double): Double {
        val h = x1 - x0
        return if(h.isTruthy()) (3 * (y1 - y0) / h - t) / 2.0
               else t
    }
}

open class MonotoneXCurve(context: Context = PathContext()) : AbstractMonotoneCurve(context) {
    var line: Double? = null

    override fun areaStart() {
        line = 0.0
    }

    override fun areaEnd() {
        line = Double.NaN
    }

    override fun lineStart() {
        x0 = Double.NaN
        y0 = Double.NaN
        x1 = Double.NaN
        y1 = Double.NaN
        point = 0
    }

    override fun lineEnd() {
        when(point) {
            2 -> context.lineTo(x1, y1)
            3 -> monotonePoint(t0, slope2(t0))
        }

        if(line.isTruthy() || (line != 0.0 && point == 1))
            context.closePath()

        line = 1 - (line ?: 0.0)
    }

    override fun point(x: Double, y: Double) {
        var t1 = Double.NaN

        if(x == x1 && y == y1)
            return // Ignore coincident points

        when(point) {
            0 -> {
                point = 1
                if(line.isTruthy())
                    context.lineTo(x, y)
                else
                    context.moveTo(x, y)
            }
            1 -> point = 2
            2 -> {
                point = 3
                t1 = slope3(x, y)
                val s0 = slope2(t1)
                monotonePoint(s0, t1)
            }
            else -> {
                t1 = slope3(x, y)
                monotonePoint(t0, t1)
            }
        }

        x0 = x1
        x1 = x
        y0 = y1
        y1 = y
        t0 = t1
    }
}

class MonotoneYCurve(context: Context = PathContext()) : MonotoneXCurve(ReflectContext(context))

private class ReflectContext(val context: Context) : Context {
    override fun moveTo(x: Double, y: Double): ReflectContext {
        context.moveTo(y, x)
        return this
    }

    override fun lineTo(x: Double, y: Double): ReflectContext {
        context.lineTo(y, x)
        return this
    }

    override fun bezierCurveTo(controlX: Double, controlY: Double, controlX2: Double, controlY2: Double, x: Double, y: Double): ReflectContext {
        context.bezierCurveTo(controlY, controlX, controlY2, controlX2, y, x)
        return this
    }

    override fun closePath(): ReflectContext {
        context.closePath()
        return this
    }

    override operator fun invoke() = context.invoke()

    override operator fun invoke(gc: GraphicsContext) = context.invoke(gc)

    override fun quadraticCurveTo(controlX: Double, controlY: Double, x: Double, y: Double): ReflectContext {
        throw UnsupportedOperationException("quadraticCurveTo not supported by ReflectContext")
    }

    override fun arcTo(x1: Double, y1: Double, x2: Double, y2: Double, radius: Double): ReflectContext {
        throw UnsupportedOperationException("arcTo not supported by ReflectContext")
    }

    override fun arc(x: Double, y: Double, r: Double, a0: Double, a1: Double, ccw: Boolean): ReflectContext {
        throw UnsupportedOperationException("arcTo not supported by ReflectContext")
    }

    override fun rect(x: Double, y: Double, w: Double, h: Double): ReflectContext {
        throw UnsupportedOperationException("rect not supported by ReflectContext")
    }
}
