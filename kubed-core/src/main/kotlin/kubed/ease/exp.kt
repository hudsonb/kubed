package kubed.ease

import javafx.animation.Interpolator
import kotlin.math.pow

class ExpInInterpolator : Interpolator() {
    override fun curve(t: Double) = 2.0.pow(10 * t - 10)
}

class ExpOutInterpolator : Interpolator() {
    override fun curve(t: Double) = 1.0 - 2.0.pow(-10.0 * t)
}

class ExpInOutInterpolator : Interpolator() {
    override fun curve(t: Double): Double {
        val t2 = t * 2
        return when {
            t2 <= 1 -> 2.0.pow(10 * t2 - 10)
            else -> 2.0 - 2.0.pow(10 - 10 * t2)
        } / 2.0
    }
}
