package kubed.delaunay

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
        val m = delaunay.halfedges.size
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

//    for(let n = hull.length, p0, x0, y0, p1 = triangles[hull[n - 1]] * 2, x1 = points[p1], y1 = points[p1 + 1], i = 0; i < n; ++i) {
//        p0 = p1, x0 = x1, y0 = y1, p1 = triangles[hull[i]] * 2, x1 = points[p1], y1 = points[p1 + 1];
//        vectors[p0 * 2 + 2] = vectors[p1 * 2] = y0 - y1;
//        vectors[p0 * 2 + 3] = vectors[p1 * 2 + 1] = x1 - x0;
//    }
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