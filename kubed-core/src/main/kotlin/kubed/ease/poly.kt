package kubed.ease

import javafx.animation.Interpolator
import kotlin.math.pow

open class PolyInInterpolator(val exponent: Double = 3.0) : Interpolator() {
    override fun curve(t: Double) = t.pow(exponent)
}

open class PolyOutInterpolator(var exponent: Double = 3.0) : Interpolator() {
    override fun curve(t: Double) =  1 - (1 - t).pow(exponent)
}

open class PolyInOutInterpolator(var exponent: Double = 3.0) : Interpolator() {
    override fun curve(t: Double): Double {
        val t2 = t * 2
        return when {
            t2 <= 1.0 -> t2.pow(exponent)
            else -> 2 - (2 - t2).pow(exponent)
        } / 2.0
    }
}