package kubed.shape.curve

import kubed.path.Context
import kubed.util.*
import java.util.*

class NaturalCurve(val context: Context) : Curve {
    var line = Double.NaN
    val x = ArrayList<Double>()
    val y = ArrayList<Double>()

    override fun areaStart() {
        line = 0.0
    }

    override fun areaEnd() {
        line = Double.NaN
    }

    override fun lineStart() {
        x.clear()
        y.clear()
    }

    override fun lineEnd() {
        val n = x.size

        if(n > 0)
        {
            if(line.isTruthy())
                context.lineTo(x[0], y[0])
            else
                context.moveTo(x[0], y[0])

            if(n == 2)
                context.lineTo(x[1], y[1])
            else {
                val px = controlPoints(x)
                val py = controlPoints(y)
                var i0 = 0
                var i1 = 1
                while(i1 < n) {
                    context.bezierCurveTo(px[0][i0], py[0][i0], px[1][i0], py[1][i0], x[i1], y[i1])
                    ++i0
                    ++i1
                }
            }
        }

        if(line.isTruthy() || (line != 0.0 && n == 1))
            context.closePath()

        line = 1 - line
        x.clear()
        y.clear()
    }

    override fun point(x: Double, y: Double) {
        this.x += x
        this.y += y
    }

    // See https://www.particleincell.com/2012/bezier-splines/ for derivation.
    fun controlPoints(x: ArrayList<Double>): Array<DoubleArray> {
        val n = x.size - 1
        val a = DoubleArray(n)
        val b = DoubleArray(n)
        val r = DoubleArray(n)

        a[0] = 0.0
        b[0] = 2.0
        r[0] = x[0] + 2 * x[1]

        for(i in 1 until n - 1) {
            a[i] = 1.0
            b[i] = 4.0
            r[i] = 4.0 * x[i] + 2.0 * x[i + 1]
        }

        a[n - 1] = 2.0
        b[n - 1] = 7.0
        r[n - 1] = 8.0 * x[n - 1] + x[n]

        for(i in 1 until n) {
            val m = a[i] / b[i - 1]
            b[i] -= m
            r[i] -= m * r[i-1]
        }
        a[n - 1] = r[n - 1] / b[n - 1]

        for(i in n - 2 downTo 0)
            a[i] = (r[i] - a[i + 1]) / b[i]

        b[n - 1] = (x[n] + a[n - 1]) / 2
        for(i in 0 until n - 1)
            b[i] = 2 * x[i + 1] - a[i + 1]

        return arrayOf(a, b)
    }
}
