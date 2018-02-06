package kubed.geo.math

/**
 * Adds floating point numbers with twice the normal precision.
 *
 * Reference: J. R. Shewchuk, Adaptive Precision Floating-Point Arithmetic and Fast Robust Geometric Predicates,
 * Discrete & Computational Geometry 18(3) 305–363 (1997).
 *
 * Code adapted from C. F. F. Karney, GeographicLib, Version 1.49 (2017-mm-dd), https://geographiclib.sourceforge.io/1.49
 */
class Accumulator {
    private var s = 0.0
    private var t = 0.0

    val sum: Double
        get() = s

    fun set(y: Double) {
        s = y
        t = 0.0
    }

    fun add(y: Double) {
        var p = sum(y, t)
        val u = p.second
        p = sum(p.first, s)
        s = p.first
        t = p.second

        if(s == 0.0) s = u
        else t += u
    }

    operator fun plusAssign(y: Double) = add(y)

    private fun sum(u: Double, v: Double): Pair<Double, Double> {
        val uv = u + v
        var up = uv - v
        var vpp = uv - up
        up -= u
        vpp -= v
        val t = -(up + vpp)
        return Pair(uv, t)
    }
}