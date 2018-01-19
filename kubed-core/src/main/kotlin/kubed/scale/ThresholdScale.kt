package kubed.scale

import kubed.array.bisect
import kotlin.comparisons.naturalOrder

/**
 * A threshold scale maps arbitrary subsets of the domain to discrete values in the range.
 *
 * The input domain is continuous, and divided into slices based on a set of threshold values.
 */
class ThresholdScale<R> : Scale<Double, R> {
    override val domain = ArrayList<Double>()
    override val range = ArrayList<R>()


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

    /**
     * Given a value in the input domain, retirns the corresponding value in the output range.
     */
    override fun invoke(d: Double): R = range[bisect(domain, d, naturalOrder(), 0, Math.min(domain.size, range.size - 1))]

    /**
     * Returns the extent of values in the domain for the corresponding value in the range, representing the inverse
     * mapping from range to domain.
     */
    fun invertExtent(y: R): List<Double> {
        val i = range.indexOf(y)
        return when(i) {
            -1 -> listOf(Double.NaN, Double.NaN)
            0 -> listOf(Double.NEGATIVE_INFINITY, domain[i])
            domain.size -> listOf(domain.last(), Double.POSITIVE_INFINITY)
            else -> listOf(domain[i - 1], domain[i])
        }
    }

    /**
     * Always returns an empty list.
     */
    override fun ticks(count: Int): List<Double> = emptyList()
}
