package kubed.scale

import kubed.array.tickStep
import kubed.array.ticks
import kubed.util.isTruthy
import kotlin.math.ceil
import kotlin.math.floor

fun <R> scaleSequential(interpolator: ((Double) -> R)? = null) = kubed.scale.scaleSequential(interpolator) {}
fun <R> scaleSequential(interpolator: ((Double) -> R)? = null, init: SequentialScale<R>.() -> Unit): SequentialScale<R> = SequentialScale(interpolator).apply(init)

class SequentialScale<R>(var interpolator: ((Double) -> R)? = null) : Scale<Double, R> {
    override val domain = ArrayList<Double>(2)
    override val range
        get() = throw UnsupportedOperationException("range is not supported by SequentialScale")

    var x0 = 0.0
    var x1 = 1.0
    var clamp = false

    fun domain(x0: Double, x1: Double) = domain(listOf(x0, x1))

    fun domain(d: List<Double>): SequentialScale<R> {
        if(d.size != 2)
            throw IllegalArgumentException("domain size must be 2")

        domain.clear()
        domain.addAll(d)

        x0 = domain.first()
        x1 = domain.last()

        return this
    }

    fun clamp(b: Boolean): SequentialScale<R> {
        clamp = b
        return this
    }

    fun interpolator(f: (Double) -> R): SequentialScale<R> {
        interpolator = f
        return this
    }

    fun nice(count: Int = 10) {
        val start = domain.first()
        val stop = domain.last()
        var step = tickStep(start, stop, count)

        if(step.isTruthy()) {
            step = tickStep(floor(start / step) * step, ceil(stop / step) * step, count)
            domain(listOf(floor(start / step) * step,
                          ceil(stop / step) * step))
        }
    }

    override fun invoke(d: Double): R {
        var t = (d - x0) / (x1 - x0)
        if(clamp)
            t = Math.max(0.0, Math.min(1.0, t))

        return interpolator?.invoke(t) ?: throw IllegalStateException("Non-null interpolator required")
    }

    override fun ticks(count: Int) = ticks(domain.first(), domain.last(), count)
}