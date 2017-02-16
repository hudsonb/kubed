package kubed.scale

import kubed.array.bisect
import kubed.array.quantile
import kotlin.comparisons.naturalOrder

fun <R> scaleQuantile(): QuantileScale<R> {
    return QuantileScale()
}

class QuantileScale<R> : Scale<Double, R> {
    override val domain = ArrayList<Double>()
    override val range = ArrayList<R>()

    private val thresholds = ArrayList<Double>()

    override fun invoke(d: Double): R {
        if(d.isNaN())
            throw IllegalArgumentException("Can not be NaN")

        return range[bisect(thresholds, d, naturalOrder())]
    }

    override fun ticks(count: Int): List<Double> = listOf()

    fun domain(d: List<Double>): QuantileScale<R> {
        domain.clear()
        domain.addAll(d.filter { it != Double.NaN })
        domain.sort()
        rescale()

        return this
    }

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

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val scale = scaleQuantile<Int>().domain(listOf(10.0, 100.0))
                                            .range(listOf(1, 2, 4))

            println(scale(20.0))
            println(scale(50.0))
            println(scale(80.0))
        }
    }

}
