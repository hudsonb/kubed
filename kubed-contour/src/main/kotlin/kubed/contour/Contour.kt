package kubed.contour

import kubed.array.range
import kubed.array.tickStep
import kubed.geo.MultiPolygon
import kubed.geo.Position
import kubed.math.LN2
import kubed.util.shl
import kubed.util.isTruthy
import kubed.util.toInt
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln

fun contours(values: DoubleArray) = contours(values, {})
fun contours(values: DoubleArray, init: ContourGenerator.() -> Unit) = ContourGenerator().apply(init).contours(values)

private typealias Point = DoubleArray
private var Point.x: Double
    get() = this[0]
    set(x) { this[0] = x }
private var Point.y: Double
    get() = this[1]
    set(y) { this[1] = y }
private fun point(x: Double, y: Double): Point = doubleArrayOf(x, y)

private typealias Line = Array<Point>
private val Line.start: Point
    get() = this[0]
private val Line.end: Point
    get() = this[1]

private fun line(start: Point, end: Point): Line = arrayOf(start, end)

private val cases = arrayOf(
        arrayOf(),
        arrayOf(line(point(1.0, 1.5), point(0.5, 1.0))),
        arrayOf(line(point(1.5, 1.0), point(1.0, 1.5))),
        arrayOf(line(point(1.5, 1.0), point(0.5, 1.0))),
        arrayOf(line(point(1.0, 0.5), point(1.5, 1.0))),
        arrayOf(line(point(1.0, 1.5), point(0.5, 1.0)), line(point(1.0, 0.5), point(1.5, 1.0))),
        arrayOf(line(point(1.0, 0.5), point(1.0, 1.5))),
        arrayOf(line(point(1.0, 0.5), point(0.5, 1.0))),
        arrayOf(line(point(0.5, 1.0), point(1.0, 0.5))),
        arrayOf(line(point(1.0, 1.5), point(1.0, 0.5))),
        arrayOf(line(point(0.5, 1.0), point(1.0, 0.5)), line(point(1.5, 1.0), point(1.0, 1.5))),
        arrayOf(line(point(1.5, 1.0), point(1.0, 0.5))),
        arrayOf(line(point(0.5, 1.0), point(1.5, 1.0))),
        arrayOf(line(point(1.0, 1.5), point(1.5, 1.0))),
        arrayOf(line(point(0.5, 1.0), point(1.0, 1.5))),
        arrayOf()
)

data class Contour(val value: Double, val geometry: MultiPolygon)

fun thresholdSturges(values: DoubleArray): DoubleArray {
    val n = (ceil(ln(values.size.toDouble()) / LN2) + 1).toInt()
    val start = values.min() ?: throw IllegalArgumentException("values array must not be empty")
    val stop = values.max() ?: throw IllegalArgumentException("values array must not be empty")
    val step = tickStep(start, stop, n)
    return range(floor(start / step) * step, floor(stop / step) * step, step)
}

class ContourGenerator {
    private var dx = 1
    private var dy = 1

    var threshold: (DoubleArray) -> DoubleArray = ::thresholdSturges

    var thresholds: DoubleArray? = null

    var columns: Int
        get() = dx
        set(value) { dx = value }

    var rows: Int
        get() = dy
        set(value) { dy = value }

    fun contours(values: DoubleArray): List<Contour> {
        require(values.size == dx * dy)

        val tz = thresholds ?: threshold(values)
        tz.sort()
        return tz.map { contour(values, it) }
    }

    private fun contour(values: DoubleArray, value: Double): Contour {
        val polygons = ArrayList<ArrayList<MutableList<Point>>>()
        val holes = ArrayList<MutableList<Point>>()

        isorings(values, value, { ring ->
            smooth(ring, values, value)
            if(area(ring) > 0) polygons.add(arrayListOf(ring))
            else holes.add(ring)
        })

        holes.forEach { hole ->
            for(polygon in polygons) {
                if(contains(polygon[0], hole)) {
                    polygon.add(hole)
                    break
                }
            }
        }

        return Contour(value, toMultiPolygon(polygons))
    }

    private data class Fragment(var start: Int, var end: Int, val ring: MutableList<Point>)

    private fun isorings(values: DoubleArray, value: Double, callback: (MutableList<Point>) -> Unit) {
        var t0: Boolean
        var t1: Boolean
        var t2: Boolean
        var t3: Boolean

        val maxSize = index(point(dx.toDouble(), dy.toDouble()))
        val fragmentByStart: Array<Fragment?> = arrayOfNulls(maxSize)
        val fragmentByEnd: Array<Fragment?> = arrayOfNulls(maxSize)

        fun threshold(index: Int) = values[index] >= value

        var x = -1
        var y = -1

        fun stitch(line: Line) {
            val start = point(line.start.x + x, line.start.y + y)
            val end = point(line.end.x + x, line.end.y + y)
            val startIndex = index(start)
            val endIndex = index(end)

            var f = fragmentByEnd[startIndex]
            var g = fragmentByStart[endIndex]
            if(f != null) {
                if(g != null) {
                    fragmentByEnd[f.end] = null
                    fragmentByStart[g.start] = null
                    if(f == g) {
                        f.ring += end
                        callback(f.ring)
                    }
                    else {
                        val ring = ArrayList<Point>(f.ring.size + g.ring.size)
                        ring += f.ring
                        ring += g.ring

                        val fragment = Fragment(f.start, g.end, ring)
                        fragmentByStart[f.start] = fragment
                        fragmentByEnd[g.end] = fragment
                    }
                }
                else {
                    fragmentByEnd[f.end] = null
                    f.ring += end
                    f.end = endIndex
                    fragmentByEnd[f.end] = f
                }
            }
            else {
                f = fragmentByStart[endIndex]
                g = fragmentByEnd[startIndex]
                if(f != null) {
                    if(g != null) {
                        fragmentByStart[f.start] = null
                        fragmentByEnd[g.end] = null
                        if(f == g) {
                            f.ring += end
                            callback(f.ring)
                        }
                        else {
                            val ring = ArrayList<Point>(g.ring.size + f.ring.size)
                            ring += g.ring
                            ring += f.ring

                            val fragment = Fragment(g.start, f.end, ring)
                            fragmentByStart[g.start] = fragment
                            fragmentByEnd[f.end] = fragment
                        }
                    }
                    else {
                        fragmentByStart[f.start] = null
                        f.ring.add(0, start)
                        f.start = startIndex
                        fragmentByStart[f.start] = f
                    }
                }
                else {
                    val fragment = Fragment(startIndex, endIndex, arrayListOf(start, end))
                    fragmentByStart[startIndex] = fragment
                    fragmentByEnd[endIndex] = fragment
                }
            }
        }

        // Special case for the first row (y = -1, t2 = t3 = false)
        t1 = threshold(0)
        cases[t1 shl 1].forEach(::stitch)
        while(++x < dx - 1) {
            t0 = t1
            t1 = threshold(x + 1)
            cases[t0.toInt() or (t1 shl 1)].forEach(::stitch)
        }
        cases[t1.toInt()].forEach(::stitch)

        // General case for the intermediate rows
        while(++y < dy - 1) {
            x = -1
            t1 = threshold(y * dx + dx)
            t2 = threshold(y * dx)
            cases[(t1 shl 1) or (t2 shl 2)].forEach(::stitch)
            while(++x < dx - 1) {
                t0 = t1
                t1 = threshold(y * dx + dx + x + 1)
                t3 = t2
                t2 = threshold(y * dx + x + 1)
                cases[t0.toInt() or (t1 shl 1) or (t2 shl 2) or (t3 shl 3)].forEach(::stitch)
            }
            cases[t1.toInt() or (t2 shl 3)].forEach(::stitch)
        }

        // Special case for the last row (y = dy - 1, t0 = t1 = false)
        x = -1
        t2 = threshold(y * dx)
        cases[t2 shl 2].forEach(::stitch)
        while(++x < dx - 1) {
            t3 = t2
            t2 = threshold(y * dx + x + 1)
            cases[(t2 shl 2) or (t3 shl 3)].forEach(::stitch)
        }
        cases[t2 shl 3].forEach(::stitch)
    }

    private fun index(point: Point): Int = (point.x * 2 + point.y * (dx + 1) * 4).toInt()

    private fun smooth(ring: List<Point>, values: DoubleArray, value: Double) {
        for(p in ring) {
            val xt = p.x.toInt()
            val yt = p.y.toInt()
            val i = yt * dx + xt
            if(i !in values.indices) continue

            var v0: Double
            val v1 = values[i]

            if(p.x > 0 && p.x < dx && p.x == xt.toDouble()) {
                v0 = if(i - 1 in values.indices) values[i - 1] else 0.0
                p.x += (value - v0) / (v1 - v0) - 0.5
            }
            if(p.y > 0 && p.y < dy && p.y == yt.toDouble()) {
                val j = (yt - 1) * dx + xt
                v0 = if(j in values.indices) values[j] else 0.0
                p.y += (value - v0) / (v1 - v0) - 0.5
            }
        }
    }

    private fun area(ring: List<Point>): Double {
        var i = 0
        val n = ring.size
        var area = ring[n - 1].y * ring[0].x - ring[n - 1].x * ring[0].y
        while(++i < n) area += ring[i - 1].y * ring[i].x - ring[i - 1].x * ring[i].y
        return area
    }

    private fun contains(ring: List<Point>, hole: List<Point>): Boolean {
        var i = -1
        var c: Int
        while(++i < hole.size) {
            c = ringContains(ring, hole[i])
            if(c.isTruthy()) return true
        }

        return false
    }

    /**
     * If point inside ring returns 1
     * If point on ring returns 0
     * If point outside ring returns -1
     */
    private fun ringContains(ring: List<Point>, point: Point): Int {
        val x = point.x
        val y = point.y
        var contains = -1
        val n = ring.size
        var j = n - 1
        var i = 0
        do {
            val pi = ring[i]
            val xi = pi.x
            val yi = pi.y
            val pj = ring[j]
            val xj = pj.x
            val yj = pj.y
            if(segmentContains(pi, pj, point)) return 0
            if(((yi > y) != (yj > y)) && ((x < (xj - xi) * (y - yi) / (yj - yi) + xi))) contains = -contains
            j = i++
        } while(i < n)

        return contains
    }

    private fun segmentContains(start: Point, end: Point, point: Point) = when {
        start.x == end.x -> within(start.y, point.y, end.y)
        else -> within(start.x, point.x, end.x)
    } && collinear(start, end, point)

    private fun collinear(a: Point, b: Point, c: Point) =
            (b.x - a.x) * (c.y - a.y) == (c.x - a.x) * (b.y - a.y)

    private fun within(p: Double, q: Double, r: Double) = q in p..r || q in r..p

    private fun toMultiPolygon(polygons: List<List<List<Point>>>) =
            MultiPolygon(polygons.map { ArrayList(it.map { ArrayList(it.map { Position(it.x, it.y) }) }) })
}