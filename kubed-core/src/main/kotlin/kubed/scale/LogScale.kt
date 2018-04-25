package kubed.scale

import kubed.array.ticks
import kubed.interpolate.Deinterpolator
import kubed.interpolate.DeinterpolatorFactory
import kubed.interpolate.Reinterpolator
import kubed.interpolate.ReinterpolatorFactory
import kubed.util.isTruthy
import java.lang.Math.*
import kotlin.math.ln

inline fun <reified R> scaleLog(
    noinline reinterpolatorFactory: ReinterpolatorFactory<R>? = null,
    noinline deinterpolatorFactory: DeinterpolatorFactory<R>? = null,
    rangeComparator: Comparator<R>? = null
): LogScale<R> = scaleLog {}

inline fun <reified R> scaleLog(
    noinline reinterpolatorFactory: ReinterpolatorFactory<R>? = null,
    noinline deinterpolatorFactory: DeinterpolatorFactory<R>? = null,
    rangeComparator: Comparator<R>? = null,
    init: LogScale<R>.() -> Unit
): LogScale<R> {
    // TODO: Default comparators

    val scale = LogScale(
        reinterpolatorFactory ?: interpolator<R>() as ReinterpolatorFactory<R>,
        when {
            deinterpolatorFactory != null -> deinterpolatorFactory
            reinterpolatorFactory != null -> null
            else -> null
        },
        rangeComparator
    )
    scale.init()
    return scale
}

class LogScale<R>(
    reinterpolatorFactory: ReinterpolatorFactory<R>,
    deinterpolatorFactory: DeinterpolatorFactory<R>? = null,
    rangeComparator: Comparator<R>? = null
) : ContinuousScale<R>(reinterpolatorFactory, deinterpolatorFactory, rangeComparator) {

    var base = 10.0
        set(value) {
            field = value
            rescale()
        }

    var logs = logp(10.0)
    var pows = powp(10.0)

    /**
     * As log(0) = -∞, a log scale domain must be strictly-positive or strictly-negative;
     * the domain must not include or cross zero. A log scale with a positive domain has a well-defined
     * behavior for positive values, and a log scale with a negative domain has a well-defined behavior for
     * negative values. (For a negative domain, input and output values are implicitly multiplied by -1.)
     * The behavior of the scale is undefined if you pass a negative value to a log scale with a positive
     * domain or vice versa.
     */
    override fun domain(d: List<Double>): ContinuousScale<R> {
        if(d.contains(0.0)) throw IllegalArgumentException("The domain must not contain 0.0, as log(0) = -∞.")
        val pos = d.filter { it > 0 }.size
        val neg = d.filter { it < 0 }.size
        if(pos > 0 && neg > 0) throw IllegalArgumentException("The domain must contain all positive or all negative elements.")

        return super.domain(d)
    }

    fun nice() {
        domain.clear()
        domain += nice(domain, { x -> pows(floor(logs(x))) }, { x -> pows(ceil(logs(x))) })
    }

    override fun deinterpolatorOf(a: Double, b: Double): Deinterpolator<Double> {
        val b2 = ln(b / a)
        return when {
            b2.isTruthy() -> { x -> ln(x / a) / b2 }
            else -> { _ -> b2 }
        }
    }

    override fun reinterpolatorOf(a: Double, b: Double): Reinterpolator<Double> = when {
        a < 0 -> { t -> -pow(-b, t) * pow(-a, 1 - t) }
        else -> { t -> pow(b, t) * pow(a, 1 - t) }
    }

    override fun rescale() {
        super.rescale()

        logs = logp(base)
        pows = powp(base)
        if(domain.first() < 0) {
            logs = reflect(logs)
            pows = reflect(pows)
        }
    }

    private fun pow10(x: Double): Double = when {
        x.isFinite() -> ("1e$x").toDouble()
        x < 0.0 -> 0.0
        else -> x
    }

    private fun powp(base: Double): (Double) -> Double = when(base) {
        10.0 -> ::pow10
        Math.E -> ::exp
        else -> { x -> pow(base, x) }
    }

    private fun logp(base: Double): (Double) -> Double = when(base) {
        Math.E -> ::ln
        10.0 -> ::log10
        else -> { x -> ln(x) / ln(base) }
    }

    private fun reflect(f: (Double) -> Double): (Double) -> Double = { x -> -f(-x) }

    override fun ticks(count: Int): List<Double> {
        var u = domain.first()
        var v = domain.last()
        val r = v < u

        if(r) {
            val temp = u
            u = v
            v = temp
        }

        var i = logs(u)
        val j = logs(v)
        var z: List<Double>? = null
        if(!(base % 1).isTruthy() && j - i < count) {
            i = (round(i) - 1).toDouble()
            if(u > 0) {
                while(i < j) {
                    val p = pows(i)
                    z = (1 until base.toInt())
                            .map { p * it }
                            .filter { it >= u }
                            .takeWhile { it <= v }
                    ++i
                }
            }
            else {
                while(i < j) {
                    val p = pows(i)
                    z = (base.toInt() - 1 downTo 1)
                            .map { p * it }
                            .filter{ it >= u }
                            .takeWhile { it <= v }

                    ++i
                }
            }
        }
        else {
            z = ticks(i, j, Math.min((j - 1).toInt(), count)).map(pows)
        }

        if(z == null)
            throw IllegalStateException("BUG: Failed to calculate ticks")

        return if(r) z.reversed() else z
    }
}
