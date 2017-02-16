package kubed.scale

import kubed.array.bisect
import kubed.interpolate.interpolateNumber
import kubed.util.isTruthy
import kotlin.comparisons.naturalOrder

fun <R> scaleQuantize(interpolate: (R, R) -> (Double) -> R,
                      uninterpolate: ((R, R) -> (R) -> Double)? = null,
                      rangeComparator: Comparator<R>? = null): QuantizeScale<R> = QuantizeScale(interpolate, uninterpolate, rangeComparator)

class QuantizeScale<R>(interpolate: (R, R) -> (Double) -> R,
                       uninterpolate: ((R, R) -> (R) -> Double)? = null,
                       rangeComparator: Comparator<R>? = null) : LinearScale<R>(interpolate, uninterpolate, rangeComparator) {
    private var x0 = 0.0
    private var x1 = 1.0

    override fun domain(d: List<Double>): QuantizeScale<R> {
        if(d.size != 2)
            throw IllegalArgumentException("QuantizeScale requires domain to consist of 2 elements")

        domain.clear()
        domain.addAll(d)
        x0 = domain.first()
        x1 = domain.last()
        rescale()

        return this
    }

    override fun range(r: List<R>): QuantizeScale<R> {
        range.clear()
        range.addAll(r)
        rescale()

        return this
    }

    override fun invoke(d: Double): R = range[bisect(domain, d, naturalOrder(), 0, range.size - 1)]

    fun invertExtent(y: R): List<Double> {
        val i = range.indexOf(y)
        val n = range.size - 1
        return when {
            i < 0 -> listOf(Double.NaN, Double.NaN)
            i < 1 -> listOf(x0, domain.first())
            i >= n -> listOf(domain[n - 1], x1)
            else -> listOf(domain[i - 1], domain[i])
        }
    }

    override fun rescale() {
        domain.clear()

        val n = range.size - 1
        for(i in range.indices) {
            domain.add(i, ((i + 1) * x1 - (i - n) * x0) / (n + 1))
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val test: Char? = null
            println(test.isTruthy())

            val scale = scaleQuantize(::interpolateNumber).domain(listOf(10.0, 100.0))
                                                           .range(listOf(1.0, 2.0, 4.0))

            println(scale(20.0))
            println(scale(50.0))
            println(scale(80.0))
        }
    }
}