package kubed.ease

import javafx.animation.Interpolator

class QuadInInterpolator : Interpolator() {
    override fun curve(t: Double) = t * t
}

class QuadOutInterpolator : Interpolator() {
    override fun curve(t: Double) = t * (2 - t)
}

class QuadInOutInterpolator : Interpolator() {
    override fun curve(t: Double): Double {
        var t2 = t * 2
        return when {
            t2 <= 1 -> t2 * t2
            else -> {
                t2 -= 1
                t2 * (2 - t2) + 1
            }
        } / 2.0
    }
}
