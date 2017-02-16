package kubed.shape.curve

import kubed.path.Context
import kubed.path.PathContext
import kubed.util.isTruthy

abstract class AbstractBasisCurve(val context: Context = PathContext()) : Curve {
    var point = 0
    var x0 = Double.NaN
    var y0 = Double.NaN
    var x1 = Double.NaN
    var y1 = Double.NaN

    override fun lineStart() {
        x0 = Double.NaN
        y0 = Double.NaN
        x1 = Double.NaN
        y1 = Double.NaN
        point = 0
    }

    fun basisPoint(x: Double, y: Double) {
        context.bezierCurveTo(
                (2 * x0 + x1) / 3,
                (2 * y0 + y1) / 3,
                (x0 + 2 * x1) / 3,
                (y0 + 2 * y1) / 3,
                (x0 + 4 * x1 + x) / 6,
                (y0 + 4 * y1 + y) / 6
        )
    }
}

class BasisCurve(context: Context = PathContext()) : AbstractBasisCurve(context) {
    var line: Double? = null

    override fun areaStart() {
        line = 0.0
    }

    override fun areaEnd() {
        line = Double.NaN
    }

    override fun lineEnd() {
       when(point) {
           2 -> context.lineTo(x1, y1)
           3 -> point(x1, y1)
       }

        if(line.isTruthy() || (line != 0.0 && point == 1))
            context.closePath()

        line = 1 - (line ?: 0.0)
    }

    override fun point(x: Double, y: Double) {
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
                context.lineTo((5 * x0 + x1) / 6, (5 * y0 + y1) / 6) // proceed
                basisPoint(x, y)
            }
            else -> basisPoint(x, y)
        }

        x0 = x1
        x1 = x
        y0 = y1
        y1 = y
    }
}

class BasisClosedCurve(context: Context = PathContext()) : AbstractBasisCurve(context) {
    var x2 = Double.NaN
    var y2 = Double.NaN
    var x3 = Double.NaN
    var y3 = Double.NaN
    var x4 = Double.NaN
    var y4 = Double.NaN

    override fun areaStart() {}
    override fun areaEnd() {}

    override fun lineEnd() {
        when(point) {
            1 -> {
                context.moveTo(x2, y2)
                context.closePath()
            }
            2 -> {
                context.moveTo((x2 + 2 * x3) / 3, (y2 + 2 * y3) / 3)
                context.lineTo((x3 + 2 * x2) / 3, (y3 + 2 * y2) / 3)
                context.closePath()
            }
            3 -> {
                point(x2, y2)
                point(x3, y3)
                point(x4, y4)
            }
        }
    }

    override fun point(x: Double, y: Double) {
        when(point) {
            0 -> {
                point = 1
                x2 = x
                y2 = y
            }
            1 -> {
                point = 2
                x3 = x
                y3 = y
            }
            2 -> {
                point = 3
                x4 = x
                y4 = y
                context.moveTo((x0 + 4 * x1 + x) / 6, (y0 + 4 * y1 + y) / 6)
            }
            else -> basisPoint(x, y)
        }

        x0 = x1
        x1 = x
        y0 = y1
        y1 = y
    }
}

class BasisOpenCurve(context: Context = PathContext()) : AbstractBasisCurve(context) {
    var line: Double? = null

    override fun areaStart() {
        line = 0.0
    }

    override fun areaEnd() {
        line = Double.NaN
    }

    override fun lineEnd() {
        if(line.isTruthy() || (line != 0.0 && point == 3))
            context.closePath()
        line = 1 - (line ?: 0.0)
    }

    override fun point(x: Double, y: Double) {
        when(point) {
            0 -> point = 1
            1 -> point = 2
            2 -> {
                point = 3
                val x0 = (x0 + 4 * x1 + x) / 6
                val y0 = (y0 + 4 * y1 + y) / 6
                if(line.isTruthy())
                    context.lineTo(x0, y0)
                else
                    context.moveTo(x0, y0)
            }
            3 -> {
                point = 4
                basisPoint(x, y)
            }
            else -> basisPoint(x, y)
        }

        x0 = x1
        x1 = x
        y0 = y1
        y1 = y
    }
}
