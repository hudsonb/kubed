package kubed.quadtree

import javafx.geometry.Point2D
import javafx.geometry.Rectangle2D
import kubed.util.shl
import kubed.util.or
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor
import kotlin.math.sqrt

class Node<T>
{
    val children = Array<Node<T>?>(4) { null }
    var data: T? = null
    var next: Node<T>? = null

    val length
        get() = children.filter { it != null }.size

    operator fun get(i: Int): Node<T>? = children[i]
    operator fun set(i: Int, node: Node<T>?) {
        children[i] = node
    }
}

data class Quad<T>(val node: Node<T>, val x0: Double, val y0: Double, val x1: Double, val y1: Double)

class Quadtree<T>(val x: (T) -> Double, val y: (T) -> Double) {
    private var x0 = Double.NaN
    private var y0 = Double.NaN
    private var x1 = Double.NaN
    private var y1 = Double.NaN

    private var root: Node<T>? = null

    val extents: Rectangle2D
        get() {
            val width = x1 - x0
            val height = y1 - y0
            if(width <= 0 || height <= 0) throw IllegalStateException("Can not calculate extent for empty tree.")
            return Rectangle2D(x0, y0, width, height)
        }

    val size: Int
        get() {
            var size = 0
            visit { node: Node<T>, _: Rectangle2D ->
                @Suppress("NAME_SHADOWING")
                var node: Node<T>? = node
                if(node?.length ?: 0 > 0) {
                    do {
                        ++size
                        node = node?.next
                    } while(node != null)
                }

                false
            }
            return size
        }

    val data: List<T>
        get() {
            val list = ArrayList<T>()
            visit { node: Node<T>, _: Rectangle2D ->
                @Suppress("NAME_SHADOWING")
                var node: Node<T>? = node
                if(node?.length ?: 0 > 0) {
                    do {
                        list += node?.data!!
                        node = node?.next
                    } while(node != null)
                }

                false
            }
            return list
        }

    fun add(d: T) {
        val x = x(d)
        val y = y(d)
        cover(x, y)
        add(x, y, d)
    }

    fun addAll(data: Collection<T>) {
        var x: Double
        var y: Double
        var x0 = Double.POSITIVE_INFINITY
        var y0 = Double.POSITIVE_INFINITY
        var x1 = Double.NEGATIVE_INFINITY
        var y1 = Double.NEGATIVE_INFINITY
        val xz = DoubleArray(data.size)
        val yz = DoubleArray(data.size)

        data.forEachIndexed { i, d ->
            x = x(d)
            y = y(d)
            xz[i] = x
            yz[i] = y
            if(x < x0) x0 = x
            else if(x > x1) x1 = x
            if(y < y0) y0 = y
            else if(y > y1) y1 = y
        }

        cover(x0, y0)
        cover(x1, y1)
        data.forEachIndexed { i, d ->
            add(xz[i], yz[i], d)
        }
    }

    fun visit(callback: (Node<T>, Rectangle2D) -> Boolean) {
        val quads = LinkedList<Quad<T>>()
        var node = root
        if(node != null) quads += Quad(node, this.x0, this.y0, this.x1, this.y1)

        var x0: Double
        var y0: Double
        var x1: Double
        var y1: Double
        var q = quads.pollLast()
        while(q != null) {
            node = q.node
            x0 = q.x0
            y0 = q.y0
            x1 = q.x1
            y1 = q.y1
            if(!callback(node, Rectangle2D(x0, y0, x1 - x0, y1 - y0)) && node.length > 0) {
                val xm = (x0 + x1) / 2
                val ym = (y0 + y1) / 2
                if(node[3] != null) quads += Quad(node[3]!!, xm, ym, x1, y1)
                if(node[2] != null) quads += Quad(node[2]!!, x0, ym, xm, y1)
                if(node[1] != null) quads += Quad(node[1]!!, xm, y0, x1, ym)
                if(node[0] != null) quads += Quad(node[0]!!, x0, y0, xm, ym)
            }
            q = quads.pollLast()
        }
    }

    fun visitAfter(callback: (Node<T>, Rectangle2D) -> Unit) {
        val quads = LinkedList<Quad<T>>()
        val next = LinkedList<Quad<T>>()
        if(root != null) quads += Quad(root!!, this.x0, this.y0, this.x1, this.y1)

        var x0: Double
        var y0: Double
        var x1: Double
        var y1: Double
        var q = quads.pollLast()
        while(q != null) {
            val node = q.node
            if(node.length > 0) {
                x0 = q.x0
                y0 = q.y0
                x1 = q.x1
                y1 = q.y1
                val xm = (x0 + x1) / 2
                val ym = (y0 + y1) / 2
                if(node[0] != null) quads += Quad(node[0]!!, x0, y0, xm, ym)
                if(node[1] != null) quads += Quad(node[1]!!, xm, y0, x1, ym)
                if(node[2] != null) quads += Quad(node[2]!!, x0, ym, xm, y1)
                if(node[3] != null) quads += Quad(node[3]!!, xm, ym, x1, y1)
            }
            next += q
            q = quads.pollLast()
        }

        next.forEach { callback(it.node, Rectangle2D(it.x0, it.y0, it.x1 - it.x0, it.y1 - it.y0)) }
    }

    fun find(x: Double, y: Double, radius: Double = Double.POSITIVE_INFINITY): T? {
        var data: T? = null
        var x0 = this.x0
        var y0 = this.y0
        var x1: Double
        var y1: Double
        var x2: Double
        var y2: Double
        var x3 = this.x1
        var y3 = this.y1
        val quads = LinkedList<Quad<T>>()
        var node = root
        @Suppress("NAME_SHADOWING")
        var radius = radius

        if(node != null) quads += Quad(node, x0, y0, x3, y3)
        if(radius.isFinite()) {
            x0 = x - radius
            y0 = y - radius
            x3 = x + radius
            y3 = y + radius
            radius *= radius
        }

        var q = quads.pollLast()
        while(q != null) {
            // Stop searching if this quadrant can't contain a closer node
            node = q.node
            x1 = q.x0
            if(x1 > x3) continue
            else {
                y1 = q.y0
                if(y1 > y3) continue
                else {
                    x2 = q.x1
                    if(x2 < x0) continue
                    else {
                        y2 = q.y1
                        if(y2 < y0) continue
                    }
                }
            }

            // Bisect the current quadrant
            if(node.length > 0) {
                val xm = (x1 + x2) / 2
                val ym = (y1 + y2) / 2

                if(node[3] != null) quads += Quad(node[3]!!, xm, ym, x2, y2)
                if(node[2] != null) quads += Quad(node[2]!!, x1, ym, xm, y2)
                if(node[1] != null) quads += Quad(node[1]!!, xm, y1, x2, ym)
                if(node[0] != null) quads += Quad(node[0]!!, x1, y1, xm, ym)

                // Visit the closest quadrant first
                val i = (y >= ym) shl 1 or (x >= xm)
                if(i != 0) {
                    q = quads.last
                    quads[quads.size - 1] = quads[quads.size - 1 - i]
                    quads[quads.size - 1 - i] = q
                }
            }
            else {
                val dx = x - x(node.data!!)
                val dy = y - y(node.data!!)
                val d2 = dx * dx + dy * dy
                if(d2 < radius) {
                    radius = d2
                    val d = sqrt(radius)
                    x0 = x - d
                    y0 = y - d
                    x3 = x + d
                    y3 = y + d
                    data = node.data
                }
            }
            q = quads.pollLast()
        }

        return data
    }

    private fun add(x: Double, y: Double, d: T) {
        var parent: Node<T>? = null
        var node = root
        val leaf = Node<T>().apply { data = d }
        var x0 = this.x0
        var y0 = this.y0
        var x1 = this.x1
        var y1 = this.y1

        if(node == null) {
            root = leaf
            return
        }

        var i = 0
        while((node?.length ?: 0) > 0) {
            val xm = (x0 + x1) / 2
            val right = x >= xm
            if(right) x0 = xm else x1 = xm
            val ym = (y0 + y1) / 2
            val bottom = y >= ym
            if(bottom) y0 = ym else y1 = ym
            parent = node
            i = (bottom shl 1) or right
            node = node?.get(i)
            if(node == null) {
                parent?.children?.set(i, leaf)
                return
            }
        }

        // Is the new point exactly coincident with the existing point?
        val xp = node?.data?.let { x(it) } ?: 0.0
        val yp = node?.data?.let { y(it) } ?: 0.0
        if(x == xp && y == yp) {
            leaf.next = node
            if(parent != null) parent[i] = leaf
            else root = leaf
            return
        }

        // Otherwise, split the lead node until the old and new points are separated
        var j: Int
        do {
            if(parent != null) {
                parent[i] = Node()
                parent = parent[i]
            }
            else {
                root = Node()
                parent = root
            }

            val xm = (x0 + x1) / 2
            val right = x >= xm
            if(right) x0 = xm else x1 = xm
            val ym = (y0 + y1) / 2
            val bottom = y >= ym
            if(bottom) y0 = ym else y1 = ym
            i = bottom shl 1 or right
            j = (yp >= ym) shl 1 or (xp >= xm)
        } while(i == j)

        parent?.set(j, node)
        parent?.set(i, leaf)
    }

    private fun cover(x: Double, y: Double) {
        var x0 = this.x0
        var y0 = this.y0
        var x1 = this.x1
        var y1 = this.y1

        if(x0.isNaN()) {
            x0 = floor(x)
            y0 = floor(y)
            x1 = x0 + 1
            y1 = y0 + 1
        }
        else if(x0 > x || x > x1 || y0 > y || y > y1) {
            var z = x1 - x0
            var node = root
            var parent: Node<T>?
            val i: Int = (y < (y0 + y1) /  2) shl 1 or (x < (x0 + x1) / 2)
            when(i) {
                0 -> {
                    do {
                        parent = Node()
                        parent[i] = node
                        node = parent
                        z *= 2
                        x1 = x0 + z
                        y1 = y0 + z
                    } while(x > x1 || y > y1)
                }
                1 -> {
                    do {
                        parent = Node()
                        parent[i] = node
                        node = parent
                        z *= 2
                        x0 = x1 - z
                        y1 = y0 + z
                    } while(x0 > x || y > y1)
                }
                2 -> {
                    do {
                        parent = Node()
                        parent[i] = node
                        node = parent
                        z *= 2
                        x1 = x0 + z
                        y0 = y1 - z
                    } while(x > x1 || y0 > y)
                }
                3 -> {
                    do {
                        parent = Node()
                        parent[i] = node
                        node = parent
                        z *= 2
                        x0 = x1 - z
                        y0 = y1 - z
                    } while(x0 > x || y0 > y)
                }
            }

            if(root != null && (root?.length ?: 0) > 0) root = node
        }
        else return

        this.x0 = x0
        this.y0 = y0
        this.x1 = x1
        this.y1 = y1
    }
}

fun main(args: Array<String>) {
    val tree = Quadtree<Point2D>({ p -> p.x }, { p -> p.y })
    tree.add(Point2D(100.0, 100.0))
    tree.add(Point2D(200.0, 200.0))

    val extent = tree.extents
    println("minX = ${extent.minX}")
    println("minY = ${extent.minY}")
    println("maxX = ${extent.maxX}")
    println("maxY = ${extent.maxY}")

    tree.visit { node: Node<Point2D>, extents: Rectangle2D ->
        if(node.length > 0) {
            println("internal")
            println("\tminX = ${extents.minX}")
            println("\tminY = ${extents.minY}")
            println("\tmaxX = ${extents.maxX}")
            println("\tmaxY = ${extents.maxY}")
        }
        else {
            println("leaf")
            println("\tminX = ${extents.minX}")
            println("\tminY = ${extents.minY}")
            println("\tmaxX = ${extents.maxX}")
            println("\tmaxY = ${extents.maxY}")
        }
        false
    }
}