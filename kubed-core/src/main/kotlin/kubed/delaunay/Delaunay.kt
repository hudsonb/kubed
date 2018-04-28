package kubed.delaunay

import javafx.geometry.Rectangle2D
import kubed.math.TAU
import kubed.path.PathContext

class Delaunay<T>(data: List<T>, x: (T) -> Double, y: (T) -> Double) {
    private val delaunator = Delaunator(data, x, y)

    val points
        get() = delaunator.points

    val halfedges
        get() = delaunator.halfedges

    val triangles
        get() = delaunator.triangles

    val hull = hullArray()

    fun voronoi(bounds: Rectangle2D) = Voronoi(this, bounds)

    fun render(context: PathContext) {
        for(i in halfedges.indices) {
            val j = halfedges[i]
            if(j < i) continue
            val ti = triangles[i] * 2
            val tj = triangles[j] * 2
            context.moveTo(points[ti], points[ti + 1])
            context.lineTo(points[tj], points[tj + 1])
        }

        renderHull(context)
    }

    fun renderPoints(context: PathContext, r: Double = 2.0) {
        var i = 0
        while(i < points.size) {
            val x = points[i]
            val y = points[i + 1]
            context.moveTo(x + r, y)
            context.arc(x, y, r, 0.0, TAU, true)
            i += 2
        }
    }

    fun renderHull(context: PathContext) {
        val n = hull.size
        var i0 = 0
        var i1 = triangles[hull[n - 1]] * 2
        for(i in hull.indices) {
            i0 = i1
            i1 = delaunator.triangles[hull[i]] * 2
            context.moveTo(delaunator.points[i0], delaunator.points[i0 + 1])
            context.lineTo(delaunator.points[i1], delaunator.points[i1 + 1])
        }
    }

    private fun hullArray(): IntArray {
        val list = ArrayList<Int>()
        var node: Node? = delaunator.hull
        do {
            list += node!!.t
            node = node!!.next
        } while(node != null && node != delaunator.hull)

        return list.toIntArray()
    }
}