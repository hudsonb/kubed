package kubed.data

import javafx.geometry.Point2D
import javafx.geometry.Rectangle2D
import kubed.util.isTruthy
import kotlin.math.*

private const val thirdPi = PI / 3

fun <T> hexbin(x: (T) -> Double, y: (T) -> Double, init: HexBin<T>.() -> Unit): HexBin<T> {
    val hexbin = HexBin(x, y)
    hexbin.init()
    return hexbin
}

class Bin<T>(val x: Double, val y: Double) {
    val values: List<T> = ArrayList()

    internal operator fun plusAssign(item: T) {
        values as ArrayList
        values.add(item)
    }
}

class HexBin<T>(val x: (T) -> Double, val y: (T) -> Double) {
    var radius = 1.0
        set(r) {
            field = r
            dx = r * 2 * Math.sin(thirdPi)
            dy = r * 1.5
        }

    var extent: Rectangle2D = Rectangle2D(0.0, 0.0, 1.0, 1.0)

    private var dx = radius * 2 * Math.sin(thirdPi)
    private var dy = radius * 1.5

    operator fun invoke(data: List<T>): List<Bin<T>> {
        val bins = HashMap<String, Bin<T>>()

        var bin: Bin<T>?

        var px: Double
        var py: Double
        for(d in data) {
            px = x(d)
            py = y(d)

            if(px.isNaN() || py.isNaN()) continue

            py /= dy
            var pj = py.roundToInt()

            px /= dx - (pj and 1) / 2
            var pi = px.roundToInt()

            val py1 = py - pj

            if(abs(py1) * 3 > 1) {
                val px1 = px - pi
                val pi2 = pi + (if(px < pi) -1 else 1) / 2
                val pj2 = pj + if(py < pj) -1 else 1
                val px2 = px - pi2
                val py2 = py - pj2
                if(px1 * px1 + py1 * py1 > px2 * px2 + py2 * py2) {
                    pi = pi2 + (if((pj and 1).isTruthy()) 1 else -1) / 2
                    pj = pj2
                }
            }

            val id = "$pi-$pj"
            bin = bins[id]
            if(bin == null) {
                bin = Bin((pi + (pj and 1) / 2.0) * dx, pj * dy)
                bins[id] = bin
            }

            bin += d
        }

        return ArrayList(bins.values)
    }

    fun centers(): List<Point2D> {
        val centers = ArrayList<Point2D>()
        var j = (extent.minY / dy).roundToInt()
        val i = (extent.minX / dx).roundToInt()

        var y = j * dy
        while(y < extent.maxY + radius) {
            var x = i * dx + (j and 1) * dx / 2
            while(x < extent.maxX + dx / 2) {
                centers += Point2D(x, y)
                x += dx
            }

            y += dy
            ++j
        }

        return centers
    }
}
