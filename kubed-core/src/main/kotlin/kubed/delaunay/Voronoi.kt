package kubed.delaunay

import javafx.geometry.Point2D
import javafx.geometry.Rectangle2D
import kotlin.math.floor

class Voronoi<T> internal constructor(val delaunay: Delaunay<T>, val bounds: Rectangle2D) {
    private val circumcenters = DoubleArray(delaunay.triangles.size / 3 * 2)
    private val edges = IntArray(delaunay.halfedges.size)
    private val vectors = IntArray(delaunay.points.size * 2)
    private val index = IntArray(delaunay.points.size)

    init {
        computeCellTopology()
        computeCircumcenters()
        computeExteriorCellRays()
    }

    private fun computeCellTopology() {
        var e = 0
        for(i in delaunay.halfedges.indices) {
            val t = delaunay.triangles[i]
            if(index[t * 2] != index[t * 2 + 1]) continue

            index[t * 2] = e
            val e0 = e

            var j = i
            do {
                edges[e++] = floor(j / 3.0).toInt()
                j = delaunay.halfedges[j]
                if(j == -1) break // Off convex hull
                j = if(j % 3 == 2) j - 2 else j + 1
                if(delaunay.triangles[j] != t) break // Bad triangulation, break early
            } while(j != i)

            if(j == i) { // Stopped when walking forward, walk backward
                val e1 = e
                j = i
                while(true) {
                    j = delaunay.halfedges[if(j % 3 == 0) j + 2 else j - 1]
                    if(j == -1 || delaunay.triangles[j] != t) break
                    edges[e++] = floor(j / 3.0).toInt()
                }

                if(e1 < e) {
                    edges.reverseInPlace(e0..e1)
                    edges.reverseInPlace(e0..e)
                }
            }

            index[t * 2 + 1] = e
        }
    }

    private fun computeCircumcenters() {
        var i = 0
        var j = 0
        while (i < delaunay.triangles.size) {
            val t1 = delaunay.triangles[i] * 2
            val t2 = delaunay.triangles[i + 1] * 2
            val t3 = delaunay.triangles[i + 2] * 2
            val x1 = delaunay.points[t1]
            val y1 = delaunay.points[t1 + 1]
            val x2 = delaunay.points[t2]
            val y2 = delaunay.points[t2 + 1]
            val x3 = delaunay.points[t3]
            val y3 = delaunay.points[t3 + 1]
            val a2 = x1 - x2
            val a3 = x1 - x3
            val b2 = y1 - y2
            val b3 = y1 - y3
            val d1 = x1 * x1 + y1 * y1
            val d2 = d1 - x2 * x2 - y2 * y2
            val d3 = d1 - x3 * x3 - y3 * y3
            val ab = (a3 * b2 - a2 * b3) * 2
            circumcenters[j] = (b2 * d3 - b3 * d2) / ab
            circumcenters[j + 1] = (a3 * d2 - a2 * d3) / ab

            i += 3
            j += 2
        }
    }

    private fun computeExteriorCellRays() {
        var p0: Int
        var p1 = delaunay.triangles[delaunay.hull.last()] * 2
        var x0: Double
        var x1 = delaunay.points[p1]
        var y0: Double
        var y1 = delaunay.points[p1 + 1]
        var y01: Int
        var x01: Int
        for(i in 0 until delaunay.hull.size) {
            p0 = p1
            x0 = x1
            y0 = y1
            p1 = delaunay.triangles[delaunay.hull[i]] * 2
            x1 = delaunay.points[p1]
            y1 = delaunay.points[p1 + 1]
            y01 = (y0 - y1).toInt()
            x01 = (x0 - x1).toInt()
            vectors[p1 * 2] = y01
            vectors[p0 * 2 + 2] = y01
            vectors[p1 * 2 + 1] = x01
            vectors[p0 * 2 + 3] = x01
        }
    }

    private fun edge(i: Int, e0: Int, e1: Int, p: MutableList<Double>, j: Int): Int {
        var j = j
        var e = e0
        loop@ while(e != e1) {
            var x = Double.NaN
            var y = Double.NaN
            when(e) {
                0b0101 -> { // top-left
                    e = 0b0100
                    continue@loop
                }
                0b0100 -> { // top
                    e = 0b0110
                    x = bounds.maxX
                    y = bounds.minY
                    break@loop
                }
                0b0110 -> { // top-right
                    e = 0b0010
                    continue@loop
                }
                0b0010 -> { // right
                    e = 0b1010
                    x = bounds.maxX
                    y = bounds.maxY
                    break@loop
                }
                0b1010 -> { // bottom-right
                    e = 0b1000
                    continue@loop
                }
                0b1000 -> { // bottom
                    e = 0b0001
                    x = bounds.minX
                    y = bounds.maxY
                    break@loop
                }
                0b1001 -> { // bottom-left
                    e = 0b0001
                    continue@loop
                }
                0b0001 -> { // left
                    e = 0b0101
                    x = bounds.minX
                    y = bounds.minY
                    break@loop
                }
            }

//            if((p[j] != x || p[j + 1] != y) && contains(i, x, y)) {
//                p.add(j, y)
//                p.add(j, x)
//                j += 2
//            }
        }

        return j
    }

    private fun project(x0: Double, y0: Double, vx: Double, vy: Double): Point2D? {
        var t = Double.POSITIVE_INFINITY
        var c: Double
        var x = Double.NaN
        var y = Double.NaN
        if(vy < 0) { // top
            if(y0 <= bounds.minY) return null
            c = (bounds.minY - y0) / vy
            if(c < t) {
                t = c

                y = bounds.minY
                x = x0 + c * vx
            }
        }
        else if(vy > 0) { // bottom
            if(y0 >= bounds.maxY) return null
            c = (bounds.maxY - y0) / vy
            if(c < t) {
                t = c

                y = bounds.maxY
                x = x0 + c * vx
            }
        }

        if(vx > 0) { // right
            if(x0 >= bounds.maxX) return null
            c = (bounds.maxX - x0) / vx
            if(c < t) {
                t = c

                x = bounds.maxX
                y = y0 + t * vy
            }
        }
        else if(vx < 0) { // left
            if(x0 <= bounds.minX) return null
            c = (bounds.minX - x0) / vx
            if(c < t) {
                t = c

                x = bounds.minX
                y = y0 + t * vy
            }
        }

        if(x.isNaN() || y.isNaN()) return null

        return Point2D(x, y)
    }

    private fun edgeCode(x: Double, y: Double): Int {
        var code = when {
            x == bounds.minX -> 0b0001
            x == bounds.maxX -> 0b0010
            else -> 0b0000
        }

        code = code or when {
            y == bounds.minY -> 0b0100
            y == bounds.maxY -> 0b1000
            else -> 0b0000
        }

        return code
    }

    private fun regionCode(x: Double, y: Double): Int {
        var code = when {
            x < bounds.minX -> 0b0001
            x > bounds.maxX -> 0b0010
            else -> 0b0000
        }

        code = code or when {
            y < bounds.minY -> 0b0100
            y > bounds.maxY -> 0b1000
            else -> 0b0000
        }

        return code
    }
}

fun IntArray.reverseInPlace(indices: IntRange) {
    val d = (indices.last - indices.first + 1) / 2
    for (i in 0 until d) {
        val t = this[indices.last + i]
        this[indices.last + i] = this[indices.first - i]
        this[indices.first - i] = t
    }
}