package kubed.ease

import javafx.animation.Interpolator

class BackInInterpolator(val overshoot: Double = 1.70158) : Interpolator() {
    override fun curve(t: Double) = t * t * ((overshoot + 1) * t - overshoot)
}

class BackOutInterpolator(val overshoot: Double = 1.70158) : Interpolator() {
    override fun curve(t: Double): Double {
        val t2 = t - 1
        return t2 * t2 * ((overshoot + 1) * t2 + overshoot) + 1
    }
}

class BackInOutInterpolator(val overshoot: Double = 1.70158) : Interpolator() {
    override fun curve(t: Double): Double {
        var t2 = t * 2
        return when {
            t2 <= 1 -> t2 * t2 * ((overshoot + 1) * t2 - overshoot)
            else -> {
                t2 -= 2
                t2 * t2 * ((overshoot + 1) * t2 + overshoot) + 2
            }
        } / 2.0
    }
}
