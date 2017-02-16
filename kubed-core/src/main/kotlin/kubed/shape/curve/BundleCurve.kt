package kubed.shape.curve

import kubed.path.Context
import kubed.path.PathContext
import java.util.*

class BundleCurve(val context: Context = PathContext(), val beta: Double) : Curve {
    private val x = ArrayList<Double>()
    private val y = ArrayList<Double>()

    private val basis = BasisCurve(context)

    override fun areaStart() = throw UnsupportedOperationException("bundle curve does not support areas")
    override fun areaEnd() = throw UnsupportedOperationException("bundle curve does not support areas")

    override fun lineStart() {
        x.clear()
        y.clear()

        basis.lineStart()
    }

    override fun lineEnd() {
        val j = x.size - 1

        if(j > 0) {
            val x0 = x[0]
            val y0 = y[0]
            val dx = x[j] - x0
            val dy = y[j] - y0
            var i = -1

            while (++i <= j) {
                val t = i / j
                basis.point(beta * x[i] + (1 - beta) * (x0 + t * dx),
                            beta * y[i] + (1 - beta) * (y0 + t * dy)
                )
            }
        }

        x.clear()
        y.clear()
        basis.lineEnd()
    }

    override fun point(x: Double, y: Double) {
        this.x += x
        this.y += y
    }
}