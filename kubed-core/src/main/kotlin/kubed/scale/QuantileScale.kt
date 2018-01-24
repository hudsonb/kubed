package kubed.scale

import kubed.array.bisect
import kubed.array.quantile
import kotlin.comparisons.naturalOrder

class QuantileScale<R> : Scale<Double, R> {
    override val domain = ArrayList<Double>()
    override val range = ArrayList<R>()

    private val thresholds = ArrayList<Double>()

    override fun invoke(d: Double): R {
        if(d.isNaN()) throw IllegalArgumentException("Can not be NaN")

        return range[bisect(thresholds, d, naturalOrder())]
    }

    override fun ticks(count: Int): List<Double> = emptyList()

    fun domain(vararg d: Double) = domain(d.toList())
    fun domain(d: List<Double>): QuantileScale<R> {
        domain.clear()
        domain.addAll(d.filter { !it.isNaN() })
        domain.sort()
        rescale()

        return this
    }

    fun range(vararg r: R) = range(r.toList())
    fun range(r: List<R>): QuantileScale<R> {
        range.clear()
        range.addAll(r)
        rescale()

        return this
    }

    fun quantiles(): List<Double> {
        return thresholds.toList()
    }

    fun invertExtent(y: R): List<Double> {
        val i = range.indexOf(y)
        return when(i) {
            0 -> listOf(Double.NaN, Double.NaN)
            else -> listOf(if(i > 0) thresholds[i - 1] else domain.first(),
                           if(i < thresholds.size) thresholds[i] else domain.last())
        }
    }

    private fun rescale() {
        var i = 0
        val n = Math.max(1, range.size)
        thresholds.clear()
        thresholds.ensureCapacity(n - 1)
        while(++i < n) {
            thresholds.add(i - 1, quantile(domain, i / n.toDouble()))
        }
    }
}
