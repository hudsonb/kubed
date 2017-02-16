package kubed.ease

import javafx.animation.Interpolator

open class PolyInInterpolator(val exponent: Double = 3.0) : Interpolator() {
    override fun curve(t: Double) = Math.pow(t, exponent)
}

open class PolyOutInterpolator(var exponent: Double = 3.0) : Interpolator() {
    override fun curve(t: Double) =  1 - Math.pow(1 - t, exponent)
}

open class PolyInOutInterpolator(var exponent: Double = 3.0) : Interpolator() {
    override fun curve(t: Double): Double {
        val t2 = t * 2
        return when {
            t2 <= 1.0 -> Math.pow(t2, exponent)
            else -> 2 - Math.pow(2 - t2, exponent)
        } / 2.0
    }
}