package kubed.shape.curve

import kubed.path.Context
import kubed.util.MoreMath
import kubed.util.isTruthy

abstract class AbstractCatmullRomCurve(val context: Context, val alpha: Double = 0.5) : Curve {
    var line = Double.NaN
    var point = 0

    var x0 = Double.NaN
    var y0 = Double.NaN
    var x1 = Double.NaN
    var y1 = Double.NaN
    var x2 = Double.NaN
    var y2 = Double.NaN
    var l01_a = 0.0
    var l01_2a = 0.0
    var l12_a = 0.0
    var l12_2a = 0.0
    var l23_a = 0.0
    var l23_2a = 0.0

    override fun lineStart() {
        x0 = Double.NaN
        x1 = Double.NaN
        x2 = Double.NaN
        y0 = Double.NaN
        y1 = Double.NaN
        y2 = Double.NaN
        l01_a = 0.0
        l12_a = 0.0
        l23_a = 0.0
        l01_2a = 0.0
        l12_2a = 0.0
        l23_2a = 0.0
        point = 0
    }

    fun catmullRomPoint(x: Double, y: Double) {
        val x2a = x2
        val y2a = y2

        if(l01_a > MoreMath.EPSILON) {
            val a = 2 * l01_2a + 3 * l01_a * l12_a + l12_2a
            val n = 3 * l01_a * (l01_a + l12_a)
            x1 = (x1 * a - x0 * l12_2a + x2 * l01_2a) / n
            y1 = (y1 * a - y0 * l12_2a + y2 * l01_2a) / n
        }

        if(l23_a > MoreMath.EPSILON) {
            val b = 2 * l23_2a + 3 * l23_a * l12_a + l12_2a
            val m = 3 * l23_a * (l23_a + l12_a)
            x2 = (x2 * b + x1 * l23_2a - x * l12_2a) / m
            y2 = (y2 * b + y1 * l23_2a - y * l12_2a) / m
        }

        context.bezierCurveTo(x1, y1, x2, y2, x2a, y2a)
    }
}

class CatmullRomCurve(context: Context, alpha: Double = 0.5) : AbstractCatmullRomCurve(context, alpha) {
    override fun areaStart() {
        line = 0.0
    }

    override fun areaEnd() {
        line = Double.NaN
    }

    override fun lineEnd() {
        when(point) {
            2 -> context.lineTo(x2, y2)
            3 -> catmullRomPoint(x2, y2)
        }

        if(line.isTruthy() || (line != 0.0 && point == 1))
            context.closePath()

        line = 1 - line
    }

    override fun point(x: Double, y: Double) {
        if(point.isTruthy()) {
            val x23 = x2 - x
            val y23 = y2 - y
            l23_2a = Math.pow(x23 * x23 + y23 * y23, alpha)
            l23_a = Math.sqrt(l23_2a)
        }

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
                catmullRomPoint(x, y)
            }
            else -> catmullRomPoint(x, y)
        }

        l01_a = l12_a
        l12_a = l23_a
        l01_2a = l12_2a
        l12_2a = l23_2a
        x0 = x1
        x1 = x2
        x2 = x
        y0 = y1
        y1 = y2
        y2 = y
    }
}

class CatmullRomClosedCurve(context: Context, alpha: Double = 0.5) : AbstractCatmullRomCurve(context, alpha) {
    var x3 = Double.NaN
    var y3 = Double.NaN
    var x4 = Double.NaN
    var y4 = Double.NaN
    var x5 = Double.NaN
    var y5 = Double.NaN

    override fun areaStart() {}
    override fun areaEnd() {}

    override fun lineStart() {
        super.lineStart()

        x3 = Double.NaN
        x4 = Double.NaN
        x5 = Double.NaN
        y3 = Double.NaN
        y4 = Double.NaN
        y5 = Double.NaN
    }

    override fun lineEnd() {
        when(point) {
            1 -> {
                context.moveTo(x3, y3)
                context.closePath()
            }
            2 -> {
                context.lineTo(x3, y3)
                context.closePath()
            }
            3 -> {
                point(x3, y3)
                point(x4, y4)
                point(x5, y5)
            }
        }
    }

    override fun point(x: Double, y: Double) {
        if(point.isTruthy()) {
            val x23 = x2 - x
            val y23 = y2 - y
            l23_2a = Math.pow(x23 * x23 + y23 * y23, alpha)
            l23_a = Math.sqrt(l23_2a)
        }

        when(point) {
            0 -> {
                point = 1
                x3 = x
                y3 = y
            }
            1 -> {
                point = 2
                x4 = x
                y4 = y
                context.moveTo(x, y)
            }
            2 -> {
                point = 3
                x5 = x
                y5 = y
            }
            else -> catmullRomPoint(x, y)
        }

        l01_a = l12_a
        l12_a = l23_a
        l01_2a = l12_2a
        l12_2a = l23_2a
        x0 = x1
        x1 = x2
        x2 = x
        y0 = y1
        y1 = y2
        y2 = y
    }
}

class CatmullRomOpenCurve(context: Context, alpha: Double = 0.5) : AbstractCatmullRomCurve(context, alpha) {
    override fun areaStart() {
        line = 0.0
    }

    override fun areaEnd() {
        line = Double.NaN
    }

    override fun lineEnd() {
        if(line.isTruthy() || (line != 0.0 && point == 3))
            context.closePath()

        line = 1 - line
    }

    override fun point(x: Double, y: Double) {
        if(point.isTruthy()) {
            val x23 = x2 - x
            val y23 = y2 - y
            l23_2a = Math.pow((x23 * x23 + y23 * y23).toDouble(), alpha)
            l23_a = Math.sqrt(l23_2a)
        }

        when(point) {
            0 -> point = 1
            1 -> point = 2
            2 -> {
                point = 3
                if(line.isTruthy())
                    context.lineTo(x2, y2)
                else
                    context.moveTo(x2, y2)
            }
            3 -> {
                point = 4
                catmullRomPoint(x, y)
            }
            else -> catmullRomPoint(x, y)
        }

        l01_a = l12_a
        l12_a = l23_a
        l01_2a = l12_2a
        l12_2a = l23_2a
        x0 = x1
        x1 = x2
        x2 = x
        y0 = y1
        y1 = y2
        y2 = y
    }
}
