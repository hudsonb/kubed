package kubed.ease

import javafx.animation.Interpolator

class BounceInInterpolator : BounceOutInterpolator() {
    override fun curve(t: Double) = 1 - super.curve(1 - t)
}

open class BounceOutInterpolator : Interpolator() {
    companion object {
        val b1 = 4.0 / 11.0
        val b2 = 6.0 / 11.0
        val b3 = 8.0 / 11.0
        val b4 = 3.0 / 4.0
        val b5 = 9.0 / 11.0
        val b6 = 10.0 / 11.0
        val b7 = 15.0 / 16.0
        val b8 = 21.0 / 22.0
        val b9 = 63.0 / 64.0
        val b0 = 1.0 / b1 / b1
    }

    override fun curve(t: Double) = when {
        t < b1 -> b0 * t * t
        t < b3 -> {
            val t2 = t - b2
            b0 * t2 * t2 + b4
        }
        t < b6 -> {
            val t2 = t - b5
            b0 * t2 * t2 + b7
        }
        else -> {
            val t2 = t - b8
            b0 * t2 * t2 + b9
        }
    }
}

class BounceInOutInterpolator : BounceOutInterpolator() {
    override fun curve(t: Double): Double {
        val t2 = t * 2
        return when {
            t2 <= 1 -> 1 - super.curve(1 - t2)
            else -> super.curve(t2 - 1) + 1
        } / 2.0
    }
}