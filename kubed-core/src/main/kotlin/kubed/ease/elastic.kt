package kubed.ease

import javafx.animation.Interpolator
import kubed.util.MoreMath

abstract class ElasticInterpolator(protected var amplitude: Double = 1.0, protected var period: Double = 0.3) : Interpolator() {
    protected var s: Double = 0.0

    init {
        this.amplitude = Math.max(1.0, amplitude)
        this.period /= MoreMath.TAU
        s = Math.asin(1 / amplitude) * period
    }

}

class ElasticInInterpolator(amplitude: Double = 1.0, period: Double = 0.3) : ElasticInterpolator(amplitude, period) {
    override fun curve(t: Double): Double {
        val t2 = t - 1
        return amplitude * Math.pow(2.0, 10 * t2) * Math.sin((s - t2) / period)
    }
}

class ElasticOutInterpolator(amplitude: Double = 1.0, period: Double = 0.3) : ElasticInterpolator(amplitude, period) {
    override fun curve(t: Double) = 1 - amplitude * Math.pow(2.0, -10 * t) * Math.sin((t + s) / period)
}

class ElasticInOutInterpolator(amplitude: Double = 1.0, period: Double = 0.3) : ElasticInterpolator(amplitude, period) {
    override fun curve(t: Double): Double {
        val t2 = t * 2 - 1
        return when {
            t2 < 0 -> amplitude * Math.pow(2.0, 10 * t2) * Math.sin((s - t2) / period)
            else -> 2 - amplitude * Math.pow(2.0, -10 * t2) * Math.sin((s + t2) / period)
        } / 2.0
    }
}
