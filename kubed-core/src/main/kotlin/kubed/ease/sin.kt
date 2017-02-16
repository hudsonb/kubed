package kubed.ease

import javafx.animation.Interpolator
import kubed.util.MoreMath

class SinInInterpolator : Interpolator() {
    override fun curve(t: Double) = 1 - Math.cos(t * MoreMath.HALF_PI)
}

class SinOutInterpolator : Interpolator() {
    override fun curve(t: Double) = Math.sin(t * MoreMath.HALF_PI)
}

class SinInOutInterpolator : Interpolator() {
    override fun curve(t: Double) = (1 - Math.cos(Math.PI * t)) / 2.0
}