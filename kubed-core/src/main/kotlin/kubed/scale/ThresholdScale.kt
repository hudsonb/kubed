package kubed.scale

import kubed.array.bisect
import kotlin.comparisons.naturalOrder

class ThresholdScale<R> : Scale<Double, R> {
    override val domain = ArrayList<Double>()
    override val range = ArrayList<R>()

    override fun invoke(d: Double): R {
        if(d.isNaN()) throw IllegalArgumentException("Can not be NaN")

        return range[bisect(domain, d, naturalOrder(), 0, Math.min(domain.size, range.size - 1))]
    }

    override fun ticks(count: Int): List<Double> = emptyList()

    fun domain(vararg d: Double) = domain(d.toList())
    fun domain(d: List<Double>): ThresholdScale<R> {
        domain.clear()
        domain.addAll(d.filter { it != Double.NaN })
        return this
    }

    fun range(vararg r: R) = range(r.toList())
    fun range(r: List<R>): ThresholdScale<R> {
        range.clear()
        range.addAll(r)
        return this
    }

    fun invertExtent(y: R): List<Double> {
        val i = range.indexOf(y)
        return when(i) {
            -1 -> listOf(Double.NaN, Double.NaN)
            0 -> listOf(Double.NEGATIVE_INFINITY, domain[i])
            domain.size -> listOf(domain.last(), Double.POSITIVE_INFINITY)
            else -> listOf(domain[i - 1], domain[i])
        }
    }
}
