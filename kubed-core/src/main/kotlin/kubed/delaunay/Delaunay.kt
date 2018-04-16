package kubed.delaunay

import javafx.geometry.Point2D
import kubed.util.isFalsy
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

class Delaunator<T>(data: List<T>, x: (T) -> Double, y: (T) -> Double) {
    private val points = Array(data.size * 2) { 0.0 }

    private var cx: Double = Double.NaN
    private var cy: Double = Double.NaN

    /**
     * ceil(sqrt(points.size))
     */
    private val hashSize: Int

    var hull: Node

    private val hash: Array<Node?>

    var triangles: IntArray

    private var halfedges: IntArray

    private var trianglesLen: Int

    init {
        var minX = Double.POSITIVE_INFINITY
        var minY = Double.POSITIVE_INFINITY
        var maxX = Double.NEGATIVE_INFINITY
        var maxY = Double.NEGATIVE_INFINITY

        val ids = IntArray(points.size)

        //points -> points
        // minX, minY, maxX, maxY
        for(i in 0 until data.size) {
            val d = data[i]
            val x = x(d)
            val y = y(d)
            ids[i] = i
            points[2 * i] = x
            points[2 * i + 1] = y
            if(x < minX) minX = x
            if(y < minY) minY = y
            if(x > maxX) maxX = x
            if(y > maxY) maxY = y
        }

        val cx = (minX + maxX) / 2
        val cy = (minY + maxY) / 2

        var minDist = Double.POSITIVE_INFINITY

        var i0: Int = -1
        var i1: Int = -1
        var i2: Int = -1

        // Pick a seed point close to the centroid
        for(i in points.indices) {
            val d = dist(cx, cy, points[2 * i], points[2 * i + 1])
            if(d < minDist) {
                i0 = i
                minDist = d
            }
        }

        minDist = Double.POSITIVE_INFINITY

        // Find the point closest to the seed
        for(i in points.indices) {
            if(i == i0) continue
            val d = dist(points[2 * i0], points[2 * i0 + 1],
                                 points[2 * i], points[2 * i + 1])
            if(d < minDist && d > 0) {
                i1 = i
                minDist = d
            }
        }

        var minRadius = Double.POSITIVE_INFINITY

        // Find the third point which forms the smallest circumcircle with the first two
        for(i in points.indices) {
            if(i == i0 || i == i1) continue

            val r = circumradius(points[2 * i0], points[2 * i0 + 1],
                                         points[2 * i1], points[2 * i1 + 1],
                                         points[2 * i], points[2 * i + 1])

            if(r < minRadius) {
                i2 = i
                minRadius = r
            }
        }

        require(minRadius != Double.POSITIVE_INFINITY) { "No Delaunay triangulation exists for this input." }

        // Swap the order of the seed points for counter-clockwise orientation
        if(area(points[2 * i0], points[2 * i0 + 1],
                points[2 * i1], points[2 * i1 + 1],
                points[2 * i2], points[2 * i2 + 1]) < 0) {
            val tmp = i1
            i1 = i2
            i2 = tmp
        }

        val i0x = points[2 * i0]
        val i0y = points[2 * i0 + 1]

        val i1x = points[2 * i1]
        val i1y = points[2 * i1 + 1]

        val i2x = points[2 * i2]
        val i2y = points[2 * i2 + 1]

        val center = circumcenter(i0x, i0y, i1x, i1y, i2x, i2y)
        this.cx = center.x
        this.cy = center.y

        // Sort the points by distance from the seed triangle circumcenter
        quicksort(ids, points, 0, ids.size - 1, center.x, center.y)

        // Initialize a hash table for storing edges of the advancing convex hull
        hashSize = ceil(sqrt(points.size.toDouble())).toInt()
        hash = arrayOfNulls(hashSize)

        // Initialize a circular doubly-linked list that will hold an advancing convex hull
        hull = insertNode(points, i0)
        var e = hull
        hashEdge(e)
        e.t = 0
        e = insertNode(points, i1, e)
        hashEdge(e)
        e.t = 1
        e = insertNode(points, i2, e)
        hashEdge(e)
        e.t = 2

        val maxTriangles = 2 * points.size - 5
        triangles = IntArray(maxTriangles * 3)
        halfedges = IntArray(maxTriangles * 3)

        trianglesLen = 0

        addTriangle(i0, i1, i2, -1, -1, -1)

        var xp:Double = Double.NEGATIVE_INFINITY
        var yp:Double = Double.NEGATIVE_INFINITY
        var i: Int
        var x: Double
        var y: Double
        for(k in ids.indices) {
            i = ids[k]
            x = points[2 * i]
            y = points[2 * i + 1]

            // Skip duplicate points
            if(x == xp && y == yp) continue
            xp = x
            yp = y

            // Skip seed triangle points
            if((x == i0x && y == i0y) ||
               (x == i1x && y == i1y) ||
               (x == i2x && y == i2y))
                continue

            // Find a visible edge on the convex hull using edge hash
            val startKey = hashKey(x, y)
            var key = startKey
            var start:Node?
            do {
                start = hash[key]
                key = (key + 1) % hashSize
            } while((start == null || start.removed) && key != startKey)

            e = start!!
            while(area(x, y, e.x, e.y, e.next!!.x, e.next!!.y) >= 0) {
                e = e.next!!
                if(e === start) throw IllegalStateException("Something is wrong with the input points.")
            }

            val walkBack = e === start

            // Add the first triangle from the point
            var t = addTriangle(e.i, i, e.next!!.i, -1, -1, e.t)

            e.t = t // Keep track of boundary triangles on the hull
            e = insertNode(points, i, e)

            // Recursively flip triangles from the point until they satisfy the Delaunay condition
            e.t = legalize(t + 2)
            if(e.prev!!.prev!!.t == halfedges[t + 1])
                e.prev!!.prev!!.t = t + 2

            // Walk forward through the hull, adding more triangles and flipping recursively
            var q = e.next
            while(area(x, y, q!!.x, q.y, q.next!!.x, q.next!!.y) < 0) {
                t = addTriangle(q.i, i, q.next!!.i, q.prev!!.t, -1, q.t)
                q.prev!!.t = legalize(t + 2)
                hull = q.remove()!!
                q = q.next
            }

            if(walkBack) {
                // Walk backward from the other side, adding more triangles and flipping
                q = e.prev
                while(area(x, y, q!!.prev!!.x, q.prev!!.y, q.x, q.y) < 0) {
                    t = addTriangle(q.prev!!.i, i, q.i, -1, q.t, q.prev!!.t)
                    legalize(t + 2)
                    q.prev!!.t = t
                    hull = q.remove()!!
                    q = q.prev
                }
            }

            // Save the two new edges in the hash table
            hashEdge(e)
            hashEdge(e.prev!!)
        }

        // Trim typed triangle mesh arrays
        triangles = triangles.copyOf(trianglesLen)
        halfedges = halfedges.copyOf(trianglesLen)
    }


    private fun hashEdge(e: Node) {
        hash[hashKey(e.x, e.y)] = e
    }

    private fun hashKey(x: Double, y: Double):Int {
        val dx = x - cx
        val dy = y - cy
        // Use pseudo-angle: a measure that monotonically increases with real angle,
        // but doesn't require expensive trigonometry
        val p: Double = 1 - dx / (abs(dx) + abs(dy))
        return floor((2.0 + if(dy < 0) -p else p) / 4 * hashSize).toInt()
    }

    private fun legalize(a: Int): Int {
        val b = halfedges[a]
        val a0 = a - a % 3
        val b0 = b - b % 3

        val al = a0 + (a + 1) % 3
        val ar = a0 + (a + 2) % 3
        val bl = b0 + (b + 2) % 3

        val p0 = triangles[ar]
        val pr = triangles[a]
        val pl = triangles[al]
        val p1 = triangles[bl]

        val illegal = withinCircle(points[2 * p0], points[2 * p0 + 1],
                points[2 * pr], points[2 * pr + 1],
                points[2 * pl], points[2 * pl + 1],
                points[2 * p1], points[2 * p1 + 1])

        if(illegal) {
            triangles[a] = p1
            triangles[b] = p0

            link(a, halfedges[bl])
            link(b, halfedges[ar])
            link(ar, bl)

            val br = b0 + (b + 1) % 3

            legalize(a)
            return legalize(br)
        }

        return ar
    }

    private fun link(a:Int, b:Int) {
        halfedges[a] = b
        if(b != -1) halfedges[b] = a
    }

    /**
     * Add a new triangle given vertex indices and adjacent half-edge ids
     */
    private fun addTriangle(i0:Int, i1:Int, i2:Int, a:Int, b:Int, c:Int): Int {
        val t = trianglesLen

        triangles[t] = i0
        triangles[t + 1] = i1
        triangles[t + 2] = i2

        link(t, a)
        link(t + 1, b)
        link(t + 2, c)

        trianglesLen += 3

        return t
    }
}


/**
 * Circular Double Linked List
 */
class Node(val i: Int,
           val x: Double,
           val y: Double,
           var t: Int,
           var prev: Node?,
           var next: Node?,
           var removed: Boolean) {
    fun remove(): Node? {
        prev?.next = next
        next?.prev = prev
        removed = true
        return prev
    }
}

// Create a new node in a doubly linked list
internal fun insertNode(points: Array<Double>, i: Int, prev: Node? = null): Node {
    val node = Node(i, points[2 * i], points[2 * i + 1], 0, null, null, false)

    when(prev) {
        null -> {
            node.prev = node
            node.next = node
        }
        else -> {
            node.next = prev.next
            node.prev = prev
            prev.next?.prev = node
            prev.next = node
        }
    }

    return node
}

fun area(ax: Double, ay: Double, bx: Double, by: Double, cx: Double, cy: Double) = (by - ay) * (cx - bx) - (bx - ax) * (cy - by)

fun withinCircle(ax: Double, ay: Double,
                 bx: Double, by: Double,
                 cx: Double, cy: Double,
                 px: Double, py: Double): Boolean {
    val pax = ax - px
    val pay = ay - py
    val pbx = bx - px
    val pby = by - py
    val pcx = cx - px
    val pcy = cy - py

    val ap = pax * pax + pay * pay
    val bp = pbx * pbx + pby * pby
    val cp = pcx * pcx + pcy * pcy

    return pax * (pby * cp - bp * pcy) -
           pay * (pbx * cp - bp * pcx) +
           ap * (pbx * pcy - pby * pcx) < 0
}

fun circumradius(ax: Double, ay: Double,
                 bx: Double, by: Double,
                 cx: Double, cy: Double): Double {
    val abx = bx - ax
    val aby = by - ay
    val acx = cx - ax
    val acy = cy - ay

    val bl = abx * abx + aby * aby
    val cl = acx * acx + acy * acy

    if (bl == 0.0 || cl == 0.0) return Double.POSITIVE_INFINITY

    val d = abx * acy - aby * acx
    if(d == 0.0) return Double.POSITIVE_INFINITY

    val x = (acy * bl - aby * cl) * 0.5 / d
    val y = (abx * cl - acx * bl) * 0.5 / d

    return x * x + y * y
}

fun circumcenter(ax: Double, ay: Double, bx: Double, by: Double, cx: Double, cy: Double): Point2D {
    val abx = bx - ax
    val aby = by - ay
    val acx = cx - ax
    val acy = cy - ay

    val bl = abx * abx + aby * aby
    val cl = acx * acx + acy * acy

    val d = abx * acy - aby * acx

    val x = (acy * bl - aby * cl) * 0.5 / d
    val y = (abx * cl - acx * bl) * 0.5 / d

    return Point2D(ax + x, ay + y)
}

inline fun Double?.orNull(): Double? = if(isFalsy()) null else this


private inline fun compare(points: Array<Double>, i: Int, j: Int, cx: Double, cy: Double): Double {
    val d1: Double = dist(points[2 * i], points[2 * i + 1], cx, cy)
    val d2: Double = dist(points[2 * j], points[2 * j + 1], cx, cy)
    return (d1 - d2).orNull() ?: (points[2 * i] - points[2 * j]) ?: (points[2 * i + 1] - points[2 * j + 1])
}

fun quicksort(ids: IntArray, points: Array<Double>, left: Int, right: Int, cx: Double, cy: Double) {
    var j: Int
    var temp: Int

    if(right - left <= 20) {
        for(i in (left + 1)..right) {
            temp = ids[i]
            j = i - 1
            while(j >= left && compare(points, ids[j], temp, cx, cy) > 0)
                ids[j + 1] = ids[j--]
            ids[j + 1] = temp
        }
    }
    else {
        var i = left + 1
        val median = (0.5 * (left + right)).toInt()
        j = right
        ids.swap(median, i)
        if(compare(points, ids[left], ids[right], cx, cy) > 0) ids.swap(left, right)
        if(compare(points, ids[i], ids[right], cx, cy) > 0) ids.swap(i, right)
        if(compare(points, ids[left], ids[i], cx, cy) > 0) ids.swap(left, i)

        temp = ids[i]
        while(true) {
            do { i++ } while(compare(points, ids[i], temp, cx, cy) < 0)
            do { j-- } while(compare(points, ids[j], temp, cx, cy) > 0)

            if(j < i) break

            ids.swap(i, j)
        }
        ids[left + 1] = ids[j]
        ids[j] = temp

        if(right - i + 1 >= j - left) {
            quicksort(ids, points, i, right, cx, cy)
            quicksort(ids, points, left, j - 1, cx, cy)
        }
        else {
            quicksort(ids, points, left, j - 1, cx, cy)
            quicksort(ids, points, i, right, cx, cy)
        }
    }
}

fun dist(ax: Double, ay: Double, bx: Double, by: Double): Double {
    val dx = ax - bx
    val dy = ay - by
    return dx * dx + dy * dy
}

private fun IntArray.swap(i: Int, j: Int) {
    val temp = this[i]
    this[i] = this[j]
    this[j] = temp
}