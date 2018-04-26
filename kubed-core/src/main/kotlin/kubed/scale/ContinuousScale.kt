package kubed.scale

import kubed.array.bisect
import kubed.interpolate.*
import java.util.*
import kotlin.comparisons.naturalOrder
import kotlin.math.min

abstract class ContinuousScale<R>(
    val reinterpolatorFactory: ReinterpolatorFactory<R>,
    val deinterpolatorFactory: DeinterpolatorFactory<R>? = null,
    val rangeComparator: Comparator<R>? = null
) : Scale<Double, R> {

    override val domain: MutableList<Double> = mutableListOf(0.0, 1.0)
    override val range: MutableList<R> = mutableListOf()

    var clamp: Boolean = false

    var piecewiseReinterpolatorFactory: PiecewiseReinterpolatorFactory<R>? = null
    var piecewiseReinterpolator: Reinterpolator<R>? = null

    var piecewiseDeinterpolatorFactory: PiecewiseDeinterpolatorFactory<R>? = null
    var piecewiseDeinterpolator: Deinterpolator<R>? = null

    /**
     * Creates an interpolator function from t in [0, 1] to x in [a, b].
     * Note: if t is not in [0, 1], x can be outside [a, b].
     */
    protected abstract fun deinterpolatorOf(a: Double, b: Double): Deinterpolator<Double>

    /**
     * Creates a deinterpolator function from x in [a, b] to t in [0, 1].
     * Note: if x is not in [a, b], t can be outside [0, 1].
     */
    protected abstract fun reinterpolatorOf(a: Double, b: Double): Reinterpolator<Double>

    /**
     * Converts the given deinterpolator factory to a clamping one:
     * t returned by factory produced deinterpolators is guaranteed to be in [0, 1].
     */
    protected fun clampDeinterpolatorFactory(deinterpolatorOf: DeinterpolatorFactory<Double>): DeinterpolatorFactory<Double> =
        { a, b ->
            val d = deinterpolatorOf(a, b);
            { x ->
                when {
                    x <= a -> 0.0
                    x >= b -> 1.0
                    else -> d(x)
                }
            }
        }

    /**
     * Converts the given interpolator factory to a clamping one:
     * x returned by factory produced interpolators is guaranteed to be in [a, b].
     */
    protected fun clampReinterpolatorFactory(reinterpolatorOf: ReinterpolatorFactory<Double>): ReinterpolatorFactory<Double> =
        { a, b ->
            val r = reinterpolatorOf(a, b);
            { t ->
                when {
                    t <= 0.0 -> a
                    t >= 1.0 -> b
                    else -> r(t)
                }
            }
        }

    open fun domain(d: List<Double>): ContinuousScale<R> {
        domain.clear()
        domain.addAll(d)
        rescale()

        return this
    }

    open fun range(r: List<R>): ContinuousScale<R> {
        range.clear()
        range.addAll(r)
        rescale()

        return this
    }

    override operator fun invoke(d: Double): R {
        if (piecewiseReinterpolator == null) {
            piecewiseReinterpolator = piecewiseReinterpolatorFactory?.invoke(
                domain,
                range,
                if (clamp)
                    clampDeinterpolatorFactory(::deinterpolatorOf)
                else
                    ::deinterpolatorOf,
                reinterpolatorFactory
            )
        }

        return piecewiseReinterpolator?.invoke(d) ?: throw IllegalStateException()
    }

    open fun invert(r: R): Double {
        if (deinterpolatorFactory == null)
            throw IllegalStateException()

        if (piecewiseDeinterpolator == null) {
            piecewiseDeinterpolator = piecewiseDeinterpolatorFactory?.invoke(range, domain,
                deinterpolatorFactory,
                if (clamp)
                    clampReinterpolatorFactory(::reinterpolatorOf)
                else
                    ::reinterpolatorOf
            )
        }

        return piecewiseDeinterpolator?.invoke(r) ?: throw IllegalStateException()
    }

    protected open fun rescale() {
        val isPoly = Math.min(domain.size, range.size) > 2
        piecewiseReinterpolatorFactory = if (isPoly) ::polymap else ::bimap
        piecewiseDeinterpolatorFactory = if (isPoly) ::polymapInvert else ::bimapInvert
        piecewiseDeinterpolator = null
        piecewiseReinterpolator = null
    }

    private fun bimap(domain: List<Double>, range: List<R>,
                      deinterpolatorOf: DeinterpolatorFactory<Double>,
                      reinterpolatorOf: ReinterpolatorFactory<R>): Reinterpolator<R> {

        val d0 = domain[0]
        val d1 = domain[1]
        val r0 = range[0]
        val r1 = range[1]

        val dt: Deinterpolator<Double>
        val tr: Reinterpolator<R>

        if (d1 < d0) {
            dt = deinterpolatorOf(d1, d0)
            tr = reinterpolatorOf(r1, r0)
        } else {
            dt = deinterpolatorOf(d0, d1)
            tr = reinterpolatorOf(r0, r1)
        }

        return { x -> tr(dt(x)) }
    }

    private fun bimapInvert(range: List<R>, domain: List<Double>,
                            deinterpolatorOf: DeinterpolatorFactory<R>,
                            reinterpolatorOf: ReinterpolatorFactory<Double>): Deinterpolator<R> {

        val r0 = range[0]
        val r1 = range[1]
        val d0 = domain[0]
        val d1 = domain[1]

        val rt: Deinterpolator<R>
        val td: Reinterpolator<Double>

        if (d1 < d0) {
            rt = deinterpolatorOf(r1, r0)
            td = reinterpolatorOf(d1, d0)
        } else {
            rt = deinterpolatorOf(r0, r1)
            td = reinterpolatorOf(d0, d1)
        }

        return { x -> td(rt(x)) }
    }

    private fun polymap(domain: List<Double>, range: List<R>,
                        deinterpolatorOf: DeinterpolatorFactory<Double>,
                        reinterpolatorOf: ReinterpolatorFactory<R>): Reinterpolator<R> {

        val d: List<Double>
        val r: List<R>

        if (domain.last() < domain.first()) {
            d = domain.reversed()
            r = range.reversed()
        } else {
            d = domain
            r = range
        }

        val n = min(domain.size, range.size) - 1 // number of segments
        val dt = Array(n, { deinterpolatorOf(d[it], d[it + 1]) }) // deinterpolators from domain segment value to t
        val tr = Array(n, { reinterpolatorOf(r[it], r[it + 1]) }) // reinterpolators from t to range segment value

        return { x ->
            val i = bisect(d, x, naturalOrder(), 1, n) - 1 // find domain segment index
            tr[i](dt[i](x))
        }
    }

    private fun polymapInvert(range: List<R>, domain: List<Double>,
                              deinterpolatorOf: DeinterpolatorFactory<R>,
                              reinterpolatorOf: ReinterpolatorFactory<Double>): Deinterpolator<R> {

        val r: List<R>
        val d: List<Double>

        if (domain.last() < domain.first()) {
            r = range.reversed()
            d = domain.reversed()
        } else {
            r = range
            d = domain
        }

        val n = Math.min(domain.size, range.size) - 1 // number of segments
        val rt = Array(n, { deinterpolatorOf(r[it], r[it + 1]) }) // deinterpolators from range segment value to t
        val td = Array(n, { reinterpolatorOf(d[it], d[it + 1]) }) // reinterpolators from t to domain segment value

        return { x ->
            val i = bisect(r, x, rangeComparator ?: throw IllegalStateException(), 1, n) - 1 // find range segment index
            td[i](rt[i](x))
        }
    }
}