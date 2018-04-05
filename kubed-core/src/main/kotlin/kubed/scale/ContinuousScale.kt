package kubed.scale

import kubed.array.bisect
import java.util.*
import kotlin.comparisons.naturalOrder
import kotlin.math.min

abstract class ContinuousScale<R>(val interpolate: (R, R) -> (Double) -> R,
                                  val uninterpolate: ((R, R) -> (R) -> Double)? = null,
                                  val rangeComparator: Comparator<R>? = null) : Scale<Double, R> {
    override val domain: MutableList<Double> = mutableListOf(0.0, 1.0)
    override val range: MutableList<R> = mutableListOf()
    var clamp: Boolean = false

    var piecewiseOutput: ((List<Double>, List<R>,
                           (Double, Double) -> (Double) -> Double,
                           (R, R) -> (Double) -> R) -> (Double) -> R)? = null
    var output: ((Double) -> R)? = null

    var piecewiseInput: ((List<Double>, List<R>,
                         (Double, Double) -> (Double) -> Double,
                         (R, R) -> (R) -> Double) -> (R) -> Double)? = null
    var input: ((R) -> Double)? = null

    protected abstract fun deinterpolate(a: Double, b: Double): (Double) -> Double

    protected abstract fun reinterpolate(a: Double, b: Double): (Double) -> Double

    protected fun deinterpolateClamp(deinterpolate: (Double, Double) -> (Double) -> Double): (Double, Double) -> (Double) -> Double {
        return fun(a: Double, b: Double): (Double) -> Double {
            val d = deinterpolate(a, b)
            return fun(x: Double): Double {
                return when {
                    x <= a -> 0.0
                    x >= b -> 1.0
                    else -> d(x)
                }
            }
        }
    }

    protected fun reinterpolateClamp(reinterpolate: (Double, Double) -> (Double) -> Double): (Double, Double) -> (Double) -> Double {
        return fun(a: Double, b: Double): (Double) -> Double {
            val r = reinterpolate(a, b)
            return fun(t: Double): Double = when {
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
        if(output == null) {
            output = piecewiseOutput?.invoke(domain,
                                       range,
                                       if(clamp) deinterpolateClamp({ a: Double, b: Double -> run { deinterpolate(a, b) } })
                                       else { a: Double, b: Double -> run { deinterpolate(a, b) } },
                                       interpolate)
        }

        return output?.invoke(d) ?: throw IllegalStateException()
    }

    open fun invert(r: R): Double {
        if(uninterpolate == null)
            throw IllegalStateException()

        if(input == null) {
            input = piecewiseInput?.invoke(domain, range,
                    if(clamp) reinterpolateClamp({ a: Double, b: Double -> run { reinterpolate(a, b) } })
                    else ::reinterpolate,
                    uninterpolate)
        }

        return input?.invoke(r) ?: throw IllegalStateException()
    }

    protected open fun rescale() {
        piecewiseOutput = if(Math.min(domain.size, range.size) > 2) ::polymap else ::bimap
        piecewiseInput = if(Math.min(domain.size, range.size) > 2) ::polymapInvert else ::bimapInvert
        input = null
        output = null
    }

    private fun bimap(domain: List<Double>, range: List<R>,
                      deinterpolate: (Double, Double) -> (Double) -> Double,
                      reinterpolate: (R, R) -> (Double) -> R): (Double) -> R {
        val d0 = domain[0]
        val d1 = domain[1]
        val r0 = range[0]
        val r1 = range[1]

        val r: (Double) -> R
        val d: (Double) -> Double

        if(d1 < d0) {
            d = deinterpolate(d1, d0)
            r = reinterpolate(r1, r0)
        }
        else {
            d = deinterpolate(d0, d1)
            r = reinterpolate(r0, r1)
        }

        return { x: Double -> r(d(x)) }
    }

    private fun bimapInvert(domain: List<Double>, range: List<R>,
                            deinterpolate: (Double, Double) -> (Double) -> Double,
                            reinterpolate: (R, R) -> (R) -> Double): (R) -> Double {
        val d0 = domain[0]
        val d1 = domain[1]
        val r0 = range[0]
        val r1 = range[1]

        val r: (R) -> Double
        val d: (Double) -> Double

        if(d1 < d0) {
            d = deinterpolate(d1, d0)
            r = reinterpolate(r1, r0)
        }
        else {
            d = deinterpolate(d0, d1)
            r = reinterpolate(r0, r1)
        }

        return { x: R -> d(r(x)) }
    }

    private fun polymap(domain: List<Double>, range: List<R>,
                        deinterpolate: (Double, Double) -> (Double) -> Double,
                        reinterpolate: (R, R) -> (Double) -> R): (Double) -> R {
        val dvalues = if(domain.last() < domain.first()) domain.reversed() else domain
        val rvalues = if(domain.last() < domain.first()) range.reversed() else range

        val j = min(domain.size, range.size) - 1
        val d = Array(j, { deinterpolate(dvalues[it], dvalues[it + 1]) })
        val r = Array(j, { reinterpolate(rvalues[it], rvalues[it + 1]) })

        return { x ->
            val idx = bisect(dvalues, x, naturalOrder(), 1, j) - 1
            r[idx](d[idx](x))
        }
    }

    private fun polymapInvert(domain: List<Double>, range: List<R>,
                              deinterpolate: (Double, Double) -> (Double) -> Double,
                              reinterpolate: (R, R) -> (R) -> Double): (R) -> Double {
        val dvalues = if(domain.last() < domain.first()) domain.reversed() else domain
        val rvalues = if(domain.last() < domain.first()) range.reversed() else range

        val j = Math.min(domain.size, range.size) - 1
        val d = Array(j, { deinterpolate(dvalues[it], dvalues[it + 1]) })
        val r = Array(j, { reinterpolate(rvalues[it], rvalues[it + 1]) })

        return { y ->
            val idx = bisect(rvalues, y, rangeComparator ?: throw IllegalStateException(), 1, j) - 1
            d[idx](r[idx](y))
        }
    }
}