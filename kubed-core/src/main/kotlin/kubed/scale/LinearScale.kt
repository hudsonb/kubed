package kubed.scale

import kubed.array.ticks
import kubed.array.tickStep
import kubed.interpolate.interpolateNumber

open class LinearScale<R>(interpolate: (R, R) -> (Double) -> R,
                          uninterpolate: ((R, R) -> (R) -> Double)? = null,
                          rangeComparator: Comparator<R>? = null) : ContinuousScale<R>(interpolate, uninterpolate, rangeComparator) {
    override fun deinterpolate(a: Double, b: Double): (Double) -> Double {
        val b2 = b - a
        return when {
            b2 == -0.0 || b2 == +0.0 || b2.isNaN() -> { _ -> b2 }
            else -> { x -> (x - a) / b2 }
        }
    }

    override fun reinterpolate(a: Double, b: Double): (Double) -> Double = interpolateNumber(a, b)

    fun domain(vararg d: Double) = domain(d.toList())
    override fun domain(d: List<Double>) = super.domain(d) as LinearScale<R>

    fun range(vararg r: R) = range(r.toList())
    override fun range(r: List<R>) = super.range(r) as LinearScale<R>

    fun nice(count: Int = 10): LinearScale<R> {
        val i = domain.size - 1
        val start: Double = domain.first()
        val stop: Double = domain.last()
        var step = tickStep(start, stop, count)

        if(step > 0) {
            step = tickStep(Math.floor(start / step) * step, Math.ceil(stop / step) * step, count)
            domain[0] = Math.floor(start / step) * step
            domain[i] = Math.ceil(stop / step) * step
        }

        return this
    }

    override fun ticks(count: Int) = ticks(domain.first(), domain.last(), count)
}
