package kubed.ease

import javafx.animation.Interpolator
import kubed.math.HALF_PI
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class SinInInterpolator : Interpolator() {
    override fun curve(t: Double) = 1 - cos(t * HALF_PI)
}

class SinOutInterpolator : Interpolator() {
    override fun curve(t: Double) = sin(t * HALF_PI)
}

class SinInOutInterpolator : Interpolator() {
    override fun curve(t: Double) = (1 - cos(PI * t)) / 2.0
}