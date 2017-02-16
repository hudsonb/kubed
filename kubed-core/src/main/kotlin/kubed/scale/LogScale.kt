package kubed.scale

import kubed.array.ticks
import kubed.util.isTruthy
import java.lang.Math.*

class LogScale<R>(interpolate: (R, R) -> (Double) -> R,
                  uninterpolate: ((R, R) -> (R) -> Double)? = null,
                  rangeComparator: Comparator<R>? = null) : ContinuousScale<R>(interpolate, uninterpolate, rangeComparator) {
    var base = 10.0
        set(value) {
            base = value
            rescale()
        }

    var logs = logp(10.0)
    var pows = powp(10.0)

    fun nice() {
        domain.clear()
        domain += nice(domain, { x -> pows(Math.floor(logs(x))) }, { x -> pows(Math.ceil(logs(x))) })
    }

    override fun deinterpolate(a: Double, b: Double): (Double) -> Double {
        val b2 = Math.log(b / a)
        return when {
            b2.isTruthy() -> { x -> Math.log(x / a) / b }
            else -> { _ -> b2 }
        }
    }

    override fun reinterpolate(a: Double, b: Double): (Double) -> Double = when {
        a < 0 -> { t -> -Math.pow(-b, t) * Math.pow(-a, 1 - t) }
        else -> { t -> Math.pow(b, t) * Math.pow(a, 1 - t)}
    }

    override fun rescale() {
        logs = logp(base)
        pows = powp(base)
        if(domain.first() < 0) {
            logs = reflect(logs)
            pows = reflect(pows)
        }
    }

    private fun pow10(x: Double): Double = when {
        x.isFinite() -> ("1e" + x).toDouble()
        x < 0.0 -> 0.0
        else -> x
    }

    private fun powp(base: Double): (Double) -> Double = when(base) {
        10.0 -> this::pow10
        Math.E -> ::exp
        else -> { x -> Math.pow(base, x) }
    }

    private fun logp(base: Double): (Double) -> Double = when(base) {
        Math.E -> ::log
        10.0 -> ::log10
        else -> { x -> Math.log(x) / Math.log(base) }
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
            i = (Math.round(i) - 1).toDouble()
            if(u > 0) {
                while(i < j) {
                    val p = pows(i)
                    z = (1..base.toInt() - 1)
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
