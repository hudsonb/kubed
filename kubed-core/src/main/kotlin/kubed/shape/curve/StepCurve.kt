package kubed.shape.curve

import kubed.path.Context
import kubed.util.isTruthy

class StepCurve(val context: Context, var t: Double = 0.5) : Curve {
    var line = Double.NaN
    var point = 0

    var x = Double.NaN
    var y = Double.NaN

    override fun areaStart() {
        line = 0.0
    }

    override fun areaEnd() {
        line = Double.NaN
    }

    override fun lineStart() {
        x = Double.NaN
        y = Double.NaN
        point = 0
    }

    override fun lineEnd() {
        if(0 < t && t < 1 && point == 2)
            context.lineTo(x, y)

        if(line.isTruthy() || (line != 0.0 && point == 1))
            context.closePath()

        if(line >= 0) {
            t = 1 - t
            line = 1 - line
        }
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
                if(t <= 0) {
                    context.lineTo(this.x, y)
                    context.lineTo(x, y)
                }
                else {
                    val x1 = this.x * (1 - t) + x * t
                    context.lineTo(x1, this.y)
                    context.lineTo(x1, y)
                }
            }
            else -> {
                if(t <= 0) {
                    context.lineTo(this.x, y)
                    context.lineTo(x, y)
                }
                else {
                    val x1 = this.x * (1 - t) + x * t
                    context.lineTo(x1, this.y)
                    context.lineTo(x1, y)
                }
            }
        }

        this.x = x
        this.y = y
    }
}
