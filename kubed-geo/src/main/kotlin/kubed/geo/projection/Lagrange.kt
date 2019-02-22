package kubed.geo.projection

import javafx.beans.property.SimpleDoubleProperty
import kubed.math.EPSILON
import kubed.math.HALF_PI
import kubed.util.isTruthy
import kotlin.math.*

fun lagrange() = lagrange {}
fun lagrange(init: LagrangeProjection.() -> Unit) = LagrangeProjection().apply {
    scale = 124.75
    init()
}

class LagrangeProjector(private val spacing: Double) : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        if(abs(abs(phi) - HALF_PI) < EPSILON) {
            return doubleArrayOf(0.0,
                                 if(phi < 0) -2.0 else 2.0)
        }

        val sinPhi = sin(phi)
        val v = ((1 + sinPhi) / (1 - sinPhi)).pow(spacing / 2)
        val lambdaSpacing = lambda * spacing
        val c = 0.5 * (v + 1 / v) + cos(lambdaSpacing)

        return doubleArrayOf(2 * sin(lambdaSpacing) / c,
                            (v - 1 / v) / c)
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val y0 = abs(y)
        if(abs(y0 - 2) < EPSILON) {
            if(x.isTruthy()) return doubleArrayOf(Double.NaN, Double.NaN)
            return doubleArrayOf(0.0, sign(y) * HALF_PI)
        }

        if(y0 > 2) return doubleArrayOf(Double.NaN, Double.NaN)

        val x = x / 2
        val y = y / 2
        val x2 = x * x
        val y2 = y * y
        var t = 2 * y / (1 + x2 + y2)
        t = ((1 + t) / (1 - t)).pow(1 / spacing)

        return doubleArrayOf(atan2(2 * x, 1 - x2 - y2) / spacing,
                             asin((t - 1) / (t + 1)))
    }
}

class LagrangeProjection : MutableProjection(LagrangeProjector(0.5)) {
    val spacingDoubleProperty = SimpleDoubleProperty(0.5)
    var spacing
        get() = spacingDoubleProperty.get()
        set(s) = spacingDoubleProperty.set(s)

    init {
        spacingDoubleProperty.addListener { _ ->
            projector = LagrangeProjector(spacing)
            invalidate()
        }
    }
}