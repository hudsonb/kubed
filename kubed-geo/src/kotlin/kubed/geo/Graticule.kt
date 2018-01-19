package kubed.geo

import kubed.array.range
import kubed.collections.slice
import kubed.math.EPSILON
import java.lang.Math.abs
import java.lang.Math.ceil

fun graticule() = graticule {}

fun graticule(init: Graticule.() -> Unit): Graticule {
    val g = Graticule()
    g.extentMajor = arrayOf(doubleArrayOf(-180.0, -90 + EPSILON), doubleArrayOf(180.0, 90 - EPSILON))
    g.extentMinor = arrayOf(doubleArrayOf(-180.0, -80 - EPSILON), doubleArrayOf(180.0, 80 + EPSILON))

    g.init()

    return g
}

class Graticule {
    private var x0 = Double.NaN
    private var x1 = Double.NaN
    private var X0 = Double.NaN
    private var X1 = Double.NaN
    private var y0 = Double.NaN
    private var y1 = Double.NaN
    private var Y0 = Double.NaN
    private var Y1 = Double.NaN
    private var dx = 10.0
    private var dy = 10.0
    private var DX = 90.0
    private var DY = 360.0
    private lateinit var x: (Double) -> List<DoubleArray>
    private lateinit var y: (Double) -> List<DoubleArray>
    private lateinit var X: (Double) -> List<DoubleArray>
    private lateinit var Y: (Double) -> List<DoubleArray>

    var precision = 2.5
        set(value) {
            x = graticuleX(y0, y1, 90.0)
            y = graticuleY(x0, x1, precision)
            X = graticuleX(Y0, Y1, 90.0)
            Y = graticuleY(X0, X1, precision)
        }

    var extent: Array<DoubleArray>
        get() = extentMinor
        set(value) {
            extentMajor = value
            extentMinor = value
        }

    var extentMajor: Array<DoubleArray>
        get() = arrayOf(doubleArrayOf(X0, Y0), doubleArrayOf(X1, Y1))
        set(value) {
            X0 = value[0][0]
            Y0 = value[0][1]
            X1 = value[1][0]
            Y1 = value[1][1]

            if(X0 > X1) {
                val t = X0
                X0 = X1
                X1 = t
            }

            if(Y0 > Y1) {
                val t = Y0
                Y0 = Y1
                Y1 = t
            }

            precision = precision
        }

    var extentMinor: Array<DoubleArray>
        get() = arrayOf(doubleArrayOf(x0, y0), doubleArrayOf(x1, y1))
        set(value) {
            x0 = value[0][0]
            y0 = value[0][1]
            x1 = value[1][0]
            y1 = value[1][1]

            if(x0 > x1) {
                val t = x0
                x0 = x1
                x1 = t
            }

            if(y0 > y1) {
                val t = y0
                y0 = y1
                y1 = t
            }

            precision = precision
        }

    var stepMajor: DoubleArray
        get() = doubleArrayOf(DX, DY)
        set(value) {
            DX = value[0]
            DY = value[1]
        }

    var stepMinor: DoubleArray
        get() = doubleArrayOf(dx, dy)
        set(value) {
            dx = value[0]
            dy = value[1]
        }

    var step: DoubleArray
        get() = stepMinor
        set(value) {
            stepMajor = value
            stepMinor = value
        }

    fun graticule() = MultiLineString(_lines().map { it.map { Position(it[0], it[1]) } })

    fun lines() = _lines().map { LineString(it.map { Position(it[0], it[1]) }) }

    fun outline(): Polygon {
        val coords = X(X0).toMutableList()
        coords += Y(Y1).slice(1)
        coords += X(X1).asReversed().slice(1)
        coords += Y(Y0).asReversed().slice(1)

        return Polygon(listOf(coords.map { Position(it[0], it[1]) } ))
    }

    private fun _lines(): List<List<DoubleArray>> {
        val lines = range(ceil(X0 / DX) * DX, X1, DX).map(X).toMutableList()
        lines += range(ceil(Y0 / DY) * DY, Y1, DY).map(Y)
        lines += range(ceil(x0 / dx) * dx, x1, dx).filter { abs(it % DX) > EPSILON }.map(x)
        lines += range(ceil(y0 / dy) * dy, y1, dy).filter { abs(it % DY) > EPSILON }.map(y)

        return lines
    }

    private fun graticuleX(y0: Double, y1: Double, dy: Double): (Double) -> List<DoubleArray> {
        val y = range(y0, y1 - EPSILON, dy).toMutableList()
        y += y1
        return { x: Double -> y.map { doubleArrayOf(x, it) } }
    }

    private fun graticuleY(x0: Double, x1: Double, dx: Double): (Double) -> List<DoubleArray> {
        val x = range(x0, x1 - EPSILON, dx).toMutableList()
        x += x1
        return { y: Double -> x.map { doubleArrayOf(it, y) } }
    }
}