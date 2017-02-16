package kubed.ease

import javafx.animation.Interpolator

class CubicInInterpolator : Interpolator() {
    override fun curve(t: Double) = t * t * t
}

class CubicOutInterpolator : Interpolator() {
    override fun curve(t: Double): Double {
        val t2 = t - 1
        return t2 * t2 * t2 - 1
    }
}

class CubicInOutInterpolator : Interpolator() {
    override fun curve(t: Double): Double {
        var t2 = t * 2
        return (when {
            t2 <= 1 -> t2 * t2 * t2
            else -> {
                t2 -= 2
                t2 * t2 * t2 + 2
            }
        }) / 2.0
    }
}