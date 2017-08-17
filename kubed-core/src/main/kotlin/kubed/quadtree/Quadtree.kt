package kubed.quadtree

import java.util.*

data class Node<T>(var topLeft: Double, var topRight: Double, var bottomLeft: Double, var bottomRight: Double, var data: T?, var next: Node<T>?) {
    val children: ArrayList<Node<T>?>? = null

    fun isLeaf() = children != null
}
data class Quad<T>(val node: Node<T>, val x0: Double, val y0: Double, val x1: Double, val y1: Double)

class Quadtree<T>(val x: (T) -> Double, val y: (T) -> Double, val x0: Double, val y0: Double, val x1: Double, val y1: Double) {
    var root: Node<T>? = null

    fun add(datum: T): Quadtree<T> {
        val x = x(datum)
        val y = y(datum)

        if(x.isNaN() || y.isNaN()) return this

        val leaf = Node(Double.NaN, Double.NaN, Double.NaN, Double.NaN, datum, null)

        // If the tree is empty, initialize the root as a leaf
        if(root == null) {
            root = leaf
            return this
        }

        var parent: Node<T>? = null
        var node = root

        var x0 = x0
        var y0 = y0
        var x1 = x1
        var y1 = y1
        var xm: Double
        var ym: Double
        var xp: Double
        var yp: Double
        var right: Boolean
        var bottom: Boolean
        var i: Int = 0
        var j: Int = 0

        // Find the existing leaf for the new point, or add it
        while(node != null && !node.isLeaf()) {
            xm = (x0 + x1) / 2
            ym = (y0 + y1) / 2

            right = x >= xm
            bottom = y >= ym

            if(right) x0 = xm else x1 = xm
            if(bottom) y0 = ym else y1 = ym

            parent = node
            i = (if(bottom) 1 else 0) shl(1) or (if(right) 1 else 0)
            node = node.children?.get(i)

            if(node != null) {
                parent.children?.set(i, leaf)
                return this
            }
        }

        // Is the new point coincident with the existing point
        if(node != null) {
            xp = x(node.data!!)
            yp = y(node.data!!)

            if (x == xp && y == yp) {
                leaf.next = node
                if (parent != null) parent.children?.set(i, leaf)
                else root = leaf
                return this
            }


            // Otherwise, split the leaf until the old and new point are separated
            do {
                val newNode = Node<T>(Double.NaN, Double.NaN, Double.NaN, Double.NaN, null, null)
                if (parent != null) parent.children?.set(i, newNode)
                else {
                    root = newNode
                    parent = root
                }

                xm = (x0 + x1) / 2
                ym = (y0 + y1) / 2
                right = x >= xm
                bottom = y >= ym
                i = (if (bottom) 1 else 0) shl (1) or (if (right) 1 else 0)
                j = (if (yp >= ym) 1 else 0) shl (1) or (if (xp >= xm) 1 else 0)
            } while(i == j)

            parent?.children?.set(j, node)
            parent?.children?.set(i, leaf)
            return this
        }

        return this
    }

    fun addAll(datum: T) {
    }

    fun remove(datum: T) {

    }

    fun removeAll(datum: T) {

    }

   fun visit(callback: (Node<T>, Double, Double, Double, Double) -> Boolean): Quadtree<T> {
       val quads = LinkedList<Quad<T>>()
       var node = root
       if(node != null) quads.push(Quad(node, x0, y0, x1, y1))

       var x0: Double
       var y0: Double
       var x1: Double
       var y1: Double

       var q = quads.pop()
       while(q != null) {
           node = q.node
           x0 = q.x0
           y0 = q.y0
           x1 = q.x1
           y1 = q.y1

           if(!callback(node, x0, y0, x1, y1)) break

           val xm = (x0 + x1) / 2
           val ym = (y0 + y1) / 2
//           if(node.children.os) {
//               quads.push(Quad(node.children[3], xm, ym, x1, y1))
//               quads.push(Quad(node.children[2], x0, ym, xm, y1))
//               quads.push(Quad(node.children[1], xm, y0, x1, ym))
//               quads.push(Quad(node.children[1], x0, y0, xm, ym))
//           }

           q = quads.pop()
       }

       return this
   }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val tree = Quadtree<Double>({ d -> d }, { d -> d }, 0.0, 0.0, 100.0, 100.0)
            tree.add(25.0)
            tree.add(95.0)
            tree.add(5.0)
        }
    }
}
