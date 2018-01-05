package kubed.scale

import kubed.array.bisect
import kotlin.comparisons.naturalOrder

class QuantizeScale<R> : LinearScale<R>({ _: R, _: R -> throw UnsupportedOperationException() }, null, null) {
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

    override fun invert(r: R): Double = throw UnsupportedOperationException("QuantizeScale does not support invert, see invertExtent")

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
}