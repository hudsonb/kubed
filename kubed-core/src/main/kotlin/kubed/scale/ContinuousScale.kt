package kubed.scale

import java.util.*
import kotlin.comparisons.naturalOrder
import kotlin.math.min
import kubed.array.bisect
import kubed.interpolate.*

abstract class ContinuousScale<R>(val _reinterpolate: InterpolatorFactory<R>,
                                  val _deinterpolate: DeinterpolatorFactory<R>? = null,
                                  val rangeComparator: Comparator<R>? = null) : Scale<Double, R> {

    override val domain: MutableList<Double> = ArrayList(2)
    override val range: MutableList<R> = ArrayList(2)

    var clamp: Boolean = false

    var piecewiseInterpolatorFactory: PiecewiseInterpolatorFactory<R>? = null
    var piecewiseInterpolator: Interpolator<R>? = null

    var piecewiseDeinterpolatorFactory: PiecewiseDeinterpolatorFactory<R>? = null
    var piecewiseDeinterpolator: Deinterpolator<R>? = null

    /**
     * Creates an interpolator function from t in [0, 1] to x in [a, b].
     * Note: if t is not in [0, 1], x can be outside [a, b].
     */
    protected abstract fun deinterpolate(a: Double, b: Double): Deinterpolator<Double>

    /**
     * Creates an deinterpolator function from x in [a, b] to t in [0, 1].
     * Note: if x is not in [a, b], t can be outside [0, 1].
     */
    protected abstract fun reinterpolate(a: Double, b: Double): Interpolator<Double>

    /**
     * Converts the given deinterpolator factory to a clamping one:
     * t returned by produced deinterpolators is guaranteed to be in [0, 1].
     */
    protected fun deinterpolateClamp(deinterpolate: DeinterpolatorFactory<Double>): DeinterpolatorFactory<Double> =
        { a, b ->
            val d = deinterpolate(a, b)
            ({ x ->
                when {
                    x <= a -> 0.0
                    x >= b -> 1.0
                    else -> d(x)
                }
            })
        }

    /**
     * Converts the given interpolator factory to a clamping one:
     * x returned by produced interpolators is guaranteed to be in [a, b].
     */
    protected fun reinterpolateClamp(reinterpolate: InterpolatorFactory<Double>): InterpolatorFactory<Double> =
        { a, b ->
            val r = reinterpolate(a, b)
            ({ t ->
                when {
                    t <= 0.0 -> a
                    t >= 1.0 -> b
                    else -> r(t)
                }
            })
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
        if (piecewiseInterpolator == null) {
            piecewiseInterpolator = piecewiseInterpolatorFactory?.invoke(
                domain,
                range,
                if (clamp) deinterpolateClamp(::deinterpolate)
                else ::deinterpolate,
                _reinterpolate
            )
        }

        return piecewiseInterpolator?.invoke(d) ?: throw IllegalStateException()
    }

    open fun invert(r: R): Double {
        if (_deinterpolate == null)
            throw IllegalStateException()

        if (piecewiseDeinterpolator == null) {
            piecewiseDeinterpolator = piecewiseDeinterpolatorFactory?.invoke(range, domain,
                _deinterpolate,
                if (clamp) reinterpolateClamp(::reinterpolate)
                else ::reinterpolate
            )
        }

        return piecewiseDeinterpolator?.invoke(r) ?: throw IllegalStateException()
    }

    protected open fun rescale() {
        val isPoly = Math.min(domain.size, range.size) > 2
        piecewiseInterpolatorFactory = if (isPoly) ::polymap else ::bimap
        piecewiseDeinterpolatorFactory = if (isPoly) ::polymapInvert else ::bimapInvert
        piecewiseDeinterpolator = null
        piecewiseInterpolator = null
    }

    private fun bimap(domain: List<Double>, range: List<R>,
                      deinterpolate: DeinterpolatorFactory<Double>,
                      reinterpolate: InterpolatorFactory<R>): Interpolator<R> {

        val d0 = domain[0]
        val d1 = domain[1]
        val r0 = range[0]
        val r1 = range[1]

        val d: Deinterpolator<Double>
        val r: Interpolator<R>

        if (d1 < d0) {
            d = deinterpolate(d1, d0)
            r = reinterpolate(r1, r0)
        } else {
            d = deinterpolate(d0, d1)
            r = reinterpolate(r0, r1)
        }

        return { x -> r(d(x)) }
    }

    private fun bimapInvert(range: List<R>, domain: List<Double>,
                            deinterpolate: DeinterpolatorFactory<R>,
                            reinterpolate: InterpolatorFactory<Double>): Deinterpolator<R> {

        val r0 = range[0]
        val r1 = range[1]
        val d0 = domain[0]
        val d1 = domain[1]

        val r: Deinterpolator<R>
        val d: Interpolator<Double>

        if (d1 < d0) {
            r = deinterpolate(r1, r0)
            d = reinterpolate(d1, d0)
        } else {
            r = deinterpolate(r0, r1)
            d = reinterpolate(d0, d1)
        }

        return { x -> d(r(x)) }
    }

    private fun polymap(domain: List<Double>, range: List<R>,
                        deinterpolate: DeinterpolatorFactory<Double>,
                        reinterpolate: InterpolatorFactory<R>): Interpolator<R> {

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
        val dt = Array(n, { deinterpolate(d[it], d[it + 1]) }) // deinterpolators from domain segment value to t
        val tr = Array(n, { reinterpolate(r[it], r[it + 1]) }) // reinterpolators from t to range segment value

        return { x ->
            val i = bisect(d, x, naturalOrder(), 1, n) - 1 // find domain segment index
            tr[i](dt[i](x))
        }
    }

    private fun polymapInvert(range: List<R>, domain: List<Double>,
                              deinterpolate: DeinterpolatorFactory<R>,
                              reinterpolate: InterpolatorFactory<Double>): Deinterpolator<R> {

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
        val rt = Array(n, { deinterpolate(r[it], r[it + 1]) }) // deinterpolators from range segment value to t
        val td = Array(n, { reinterpolate(d[it], d[it + 1]) }) // reinterpolators from t to domain segment value

        return { x ->
            val i = bisect(r, x, rangeComparator ?: throw IllegalStateException(), 1, n) - 1 // find range segment index
            td[i](rt[i](x))
        }
    }
}