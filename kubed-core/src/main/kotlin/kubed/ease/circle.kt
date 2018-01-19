package kubed.ease

import javafx.animation.Interpolator
import kotlin.math.sqrt

class CircleInInterpolator : Interpolator() {
    override fun curve(t: Double) = 1 - sqrt(1 - t * t)
}

class CircleOutInterpolator : Interpolator() {
    override fun curve(t: Double): Double {
        val t2 = t - 1
        return sqrt(1 - t2 * t2)
    }
}

class CircleInOutInterpolator : Interpolator() {
    override fun curve(t: Double): Double {
        var t2 = t * 2
        return when {
            t2 <= 1 -> 1 - sqrt(1 - t2 * t2)
            else -> {
                t2 -= 2
                sqrt(1 - t2 * t2) + 1
            }
        } / 2.0
    }
}