package kubed.ease

import javafx.animation.Interpolator

class LinearInterpolator : Interpolator() {
    override fun curve(t: Double) = t
}