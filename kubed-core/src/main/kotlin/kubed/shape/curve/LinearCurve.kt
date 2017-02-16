package kubed.shape.curve

import kubed.path.Context
import kubed.path.PathContext
import kubed.util.isTruthy

class LinearCurve(val context: Context = PathContext()) : Curve {
    var line: Double = Double.NaN
    var point = 0

    override fun areaStart() {
        line = 0.0
    }

    override fun areaEnd() {
        line = Double.NaN
    }

    override fun lineStart() {
        point = 0
    }

    override fun lineEnd() {
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
                context.lineTo(x, y)
            }

            else -> context.lineTo(x, y)
        }
    }
}

class LinearClosedCurve(val context: Context = PathContext()) : Curve {
    var point = 0

    override fun areaStart() {}

    override fun areaEnd() {}

    override fun lineStart() {
        point = 0
    }

    override fun lineEnd() {
        if(point.isTruthy())
            context.closePath()
    }

    override fun point(x: Double, y: Double) {
        if(point.isTruthy()) {
            context.lineTo(x, y)
        }
        else {
            point = 1
            context.moveTo(x, y)
        }
    }
}
