package kubed.geo.projection

import kubed.util.isTruthy
import kotlin.math.*

fun craig() = craig {}
fun craig(init: Parallel1Projection.() -> Unit) = Parallel1Projection(::CraigProjector).apply {
    scale = 152.63
    init()
}

class CraigProjector(phi0: Double) : InvertableProjector {
    private val tanPhi0 = tan(phi0)

    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        return doubleArrayOf(lambda,
                (if(lambda.isTruthy()) lambda / sin(lambda) else 1.0) * (sin(phi) * cos(lambda) - tanPhi0 * cos(phi)))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        if(tanPhi0.isTruthy()) {
            var y = y
            if (x.isTruthy()) y *= sin(x) / x
            val cosLambda = cos(x)
            return doubleArrayOf(x, 2 * atan2(sqrt(cosLambda * cosLambda + tanPhi0 * tanPhi0 - y * y) - cosLambda, tanPhi0 - y))
        }

        return doubleArrayOf(x, asin(if(x.isTruthy()) y * tan(x) / x else y))
    }
}