package kubed.contour

import kubed.array.range
import kubed.array.tickStep
import kubed.geo.MultiPolygon
import kubed.geo.Position
import java.lang.Math.pow
import java.lang.Math.round
import kotlin.math.*

fun <T> contourDensity(x: (T, Int, List<T>) -> Double, y: (T, Int, List<T>) -> Double) = contourDensity(x, y) {}
fun <T> contourDensity(x: (T, Int, List<T>) -> Double, y: (T, Int, List<T>) -> Double, init: ContourDensityEstimator<T>.() -> Unit): ContourDensityEstimator<T> {
    return ContourDensityEstimator(x, y).apply(init)
}

class ContourDensityEstimator<T>(var x: (T, Int, List<T>) -> Double, var y: (T, Int, List<T>) -> Double) {
    var width = 960.0
    set(w) {
        field = ceil(w)
        resize()
    }

    var height = 500.0
        set(h) {
            field = ceil(h)
            resize()
        }

    var threshold: ((DoubleArray) -> DoubleArray)? = null
    var thresholds: DoubleArray? = null
    var thresholdCount = 20

    var cellSize: Int
        get() = 1 shl k
        set(value) {
            k = floor(ln(value.toDouble())).toInt()
            resize()
        }

    var bandwidth: Double
        get() = sqrt(r * (r + 1).toDouble())
        set(value) {
            r = round((sqrt(4 * value * value + 1) - 1) / 2.0).toInt()
            resize()
        }

    private var r = 20 // Blur radius
    private var k = 2 // log2(grid cell size)
    private var o = 0 // Grid offset, to pad for blur - set in resize()
    private var n = 0 // gridWidth - set in resize()
    private var m = 0 // gridHeight - set in resize()

    init {
        resize()
    }

    operator fun invoke(data: List<T>): List<Contour> {
        val values0 = DoubleArray(n * m)
        val values1 = DoubleArray(n * m)

        var d: T
        for(i in data.indices) {
            d = data[i]
            val xi = (x(d, i, data) + o).toInt() shr k
            val yi = (y(d, i, data) + o).toInt() shr k
            if(xi in 0..(n - 1) && yi in 0..(m - 1)) {
                ++values0[xi + yi * n]
            }
        }

        val rshrk = r shr k
        blurX(n, m, values0, values1, rshrk)
        blurY(n, m, values1, values0, rshrk)
        blurX(n, m, values0, values1, rshrk)
        blurY(n, m, values1, values0, rshrk)
        blurX(n, m, values0, values1, rshrk)
        blurY(n, m, values1, values0, rshrk)

        val tz = when {
            thresholds != null -> thresholds!!
            threshold != null -> threshold!!.invoke(values0)
            else -> {
                val stop = values0.max()!!
                val step = tickStep(0.0, stop, thresholdCount)
                val t = range(0.0, floor(stop / step) * step, step)
                t.drop(1).toDoubleArray()
            }
        }

        val k2 = Math.pow(2.0, k.toDouble())
        return contours(values0) {
            columns = n
            rows = m
            this.thresholds = tz
        }.map { c ->
            val geo = MultiPolygon(c.geometry.coordinates.map {
                it.map {
                    it.map {
                        Position(it.longitude * k2 - o,
                                it.latitude * k2 - o)
                    }
                }
            })
            Contour(c.value * pow(2.0, -2.0 * k), geo)
        }
    }

    private fun resize() {
        o = r * 3
        n = (width + o * 2).toInt() shr k
        m = (height + o * 2).toInt() shr k
    }

    private fun blurX(n: Int, m: Int, source: DoubleArray, target: DoubleArray, r: Int) {
        val w = (r shl 1) + 1

        for(j in 0 until m) {
            var sr = 0.0
            for(i in 0 until (n + r)) {
                if(i < n) sr += source[i + j * n]
                if(i >= r) {
                    if(i >= w) sr -= source[i - w + j * n]
                    target[i - r + j * n] = sr / min(min(i + 1, n - 1 + w - i), w)
                }
            }
        }
    }

    private fun blurY(n: Int, m: Int, source: DoubleArray, target: DoubleArray, r: Int) {
        val w = (r shl 1) + 1

        for(i in 0 until n) {
            var sr = 0.0
            for(j in 0 until (m + r)) {
                if(j < m) sr += source[i + j * n]
                if(j >= r) {
                    if(j >= w) sr -= source[i + (j - w) * n]
                    target[i + (j - r) * n] = sr / min(min(j + 1, m - 1 + w - j), w)
                }
            }
        }
    }
}