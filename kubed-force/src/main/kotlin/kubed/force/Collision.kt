package kubed.force

import javafx.geometry.Rectangle2D
import kubed.quadtree.InternalNode
import kubed.quadtree.LeafNode
import kubed.quadtree.Quadtree
import kubed.quadtree.QuadtreeNode
import kubed.util.isFalsy
import kotlin.math.sqrt

fun forceCollision(radius: Double) = forceCollision(radius) {}
fun forceCollision(radius: Double, init: Collision.() -> Unit) = forceCollision(constant(radius), init)

fun forceCollision(radius: (ForceNode, Int, List<ForceNode>) -> Double) = forceCollision(radius) {}
fun forceCollision(radius: (ForceNode, Int, List<ForceNode>) -> Double, init: Collision.() -> Unit) = Collision(radius).apply(init)

class Collision(var radius: (ForceNode, Int, List<ForceNode>) -> Double = constant(1.0)) : Force {
    private val nodes = ArrayList<ForceNode>()

    private val x = { node: ForceNode -> node.x + node.vx }
    private val y = { node: ForceNode -> node.y + node.vy }

    var iterations = 1

    var strength = 1.0

    override fun initialize(nodes: List<ForceNode>) {
        this.nodes.clear()
        this.nodes.addAll(nodes)

        nodes.forEach { node ->
            node.radius = radius(node, node.index, nodes)
            //println(node.radius)
        }
    }

    override fun invoke(alpha: Double) {
        for(k in 0 until iterations) {
            val tree = Quadtree(x, y, nodes)
            tree.visitAfter(::prepare)

            nodes.filter { it.radius > 0.0 }.forEach { println(it) }
            for(node in nodes) {
                val ri = node.radius
                val ri2 = ri * ri
                val xi = node.x + node.vx
                val yi = node.y + node.vx

                tree.visit { quad, extents ->
                    var rj = quad.r
                    var r = ri + rj
                    if(quad is LeafNode<ForceNode> && quad.data != null) {
                        val data = quad.data!!
                        if(data.index > node.index) {
                            var x = xi - data.x - data.vx
                            var y = yi - data.y - data.vy
                            var l = x * x + y * y
                            if(l < r * r) {
                                if(x.isFalsy()) {
                                    x = jiggle()
                                    l += x * x
                                }

                                if(y.isFalsy()) {
                                    y = jiggle()
                                    l += y * y
                                }

                                l = sqrt(l)
                                l = (r - l) / l * strength

                                x *= l
                                rj *= rj
                                r = rj / (ri2 + rj)
                                node.vx += x * r
                                y *= l
                                node.vy += y * r
                                r = 1 - r
                                data.vx -= x * r
                                data.vy -= y * r
                            }
                        }
                        false
                    }
                    else extents.minX > xi + r || extents.maxX < xi - r || extents.minY > yi + r || extents.maxY < yi - r
                }
            }
        }
    }

    private fun prepare(quad: QuadtreeNode<ForceNode>, @Suppress("UNUSED_PARAMETER") extents: Rectangle2D) {
        when(quad) {
            is LeafNode -> if(quad.data != null) quad.r = quad.data?.radius ?: 0.0
            is InternalNode -> {
                quad.r = 0.0
                for(q in quad.children) {
                    if(q != null && q.r > quad.r) quad.r = q.r
                }
            }
        }
    }

    private var ForceNode.radius: Double
        get() = properties["radius"] as Double? ?: Double.NaN
        set(value) {
            properties["radius"] = value
        }

    private var QuadtreeNode<ForceNode>.r: Double
        get() = properties["r"] as Double? ?: 0.0
        set(value) {
            properties["r"] = value
        }
}