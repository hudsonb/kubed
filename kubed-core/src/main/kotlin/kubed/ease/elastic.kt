package kubed.ease

import javafx.animation.Interpolator
import kubed.math.TAU
import kotlin.math.asin
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

abstract class ElasticInterpolator(protected var amplitude: Double = 1.0, protected var period: Double = 0.3) : Interpolator() {
    protected var s: Double = 0.0

    init {
        this.amplitude = max(1.0, amplitude)
        this.period /= TAU
        s = asin(1 / amplitude) * period
    }

}

class ElasticInInterpolator(amplitude: Double = 1.0, period: Double = 0.3) : ElasticInterpolator(amplitude, period) {
    override fun curve(t: Double): Double {
        val t2 = t - 1
        return amplitude * 2.0.pow(10 * t2) * sin((s - t2) / period)
    }
}

class ElasticOutInterpolator(amplitude: Double = 1.0, period: Double = 0.3) : ElasticInterpolator(amplitude, period) {
    override fun curve(t: Double) = 1 - amplitude * 2.0.pow(-10 * t) * sin((t + s) / period)
}

class ElasticInOutInterpolator(amplitude: Double = 1.0, period: Double = 0.3) : ElasticInterpolator(amplitude, period) {
    override fun curve(t: Double): Double {
        val t2 = t * 2 - 1
        return when {
            t2 < 0 -> amplitude * 2.0.pow(10 * t2) * sin((s - t2) / period)
            else -> 2 - amplitude * 2.0.pow(-10 * t2) * sin((s + t2) / period)
        } / 2.0
    }
}
