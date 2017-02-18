package kubed.shape.curve

import kubed.path.Context
import kubed.util.isTruthy

abstract class AbstractCardinalCurve(val context: Context, tension: Double = 0.0) : Curve {
    var k = (1.0 - tension) / 6.0
    var point = 0
    var line = Double.NaN

    var x0 = Double.NaN
    var y0 = Double.NaN
    var x1 = Double.NaN
    var y1 = Double.NaN
    var x2 = Double.NaN
    var y2 = Double.NaN

    fun cardinalPoint(x: Double, y: Double) {
        context.bezierCurveTo(x1 + k * (x2 - x0),
                              y1 + k * (y2 - y0),
                              x2 + k * (x1 - x),
                              y2 + k * (y1 - y),
                              x2,
                              y2)
    }

    override fun lineStart() {
        x0 = Double.NaN
        x1 = Double.NaN
        x2 = Double.NaN
        y0 = Double.NaN
        y1 = Double.NaN
        y2 = Double.NaN
        point = 0
    }
}

class CardinalCurve(context: Context, tension: Double = 0.0) : AbstractCardinalCurve(context, tension) {
    override fun areaStart() {
        line = 0.0
    }

    override fun areaEnd() {
        line = Double.NaN
    }

    override fun lineEnd() {
        when(point) {
            2 -> context.lineTo(x2, y2)
            3 -> cardinalPoint(x1, y1)
        }

        if(line.isTruthy() || (line != 0.0 && point == 1))
            context.closePath()

        line = 1 - line
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
            1 -> {
                point = 2
                x1 = x
                y1 = y
            }
            2 -> {
                point = 3
                cardinalPoint(x, y)
            }
            else -> cardinalPoint(x, y)
        }

        x0 = x1
        x1 = x2
        x2 = x
        y0 = y1
        y1 = y2
        y2 = y
    }
}

class CardinalClosedCurve(context: Context, tension: Double = 0.0) : AbstractCardinalCurve(context, tension) {
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
        y3 = Double.NaN
        x4 = Double.NaN
        y4 = Double.NaN
        x5 = Double.NaN
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
            else -> cardinalPoint(x, y)
        }

        x0 = x1
        x1 = x2
        x2 = x
        y0 = y1
        y1 = y2
        y2 = y
    }
}

class CardinalOpenCurve(context: Context, tension: Double = 0.0) : AbstractCardinalCurve(context, tension) {
    override fun areaStart() {
        line = 0.0
    }

    override fun areaEnd() {
        line = Double.NaN
    }

    override fun lineEnd() {
        if(line.isTruthy() || (line == 0.0 && point == 3))
            context.closePath()

        line = 1 - line
    }

    override fun point(x: Double, y: Double) {
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
                cardinalPoint(x, y)
            }
            else -> cardinalPoint(x, y)
        }

        x0 = x1
        x1 = x2
        x2 = x
        y0 = y1
        y1 = y2
        y2 = y
    }

}
