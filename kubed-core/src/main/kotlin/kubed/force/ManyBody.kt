package kubed.force

import javafx.geometry.Rectangle2D
import kubed.quadtree.InternalNode
import kubed.quadtree.LeafNode
import kubed.quadtree.Quadtree
import kubed.quadtree.QuadtreeNode
import kubed.util.isFalsy
import kubed.util.isNotNaN
import kotlin.math.abs
import kotlin.math.sqrt

fun forceManyBody(init: ManyBody.() -> Unit) = ManyBody().apply(init)

class ManyBody : Force {
    private var distanceMin2 = 1.0
    private var distanceMax2 = Double.POSITIVE_INFINITY
    private var theta2 = 0.81

    private val nodes = ArrayList<ForceNode>()

    var strength: (node: ForceNode, index: Int, nodes: List<ForceNode>) -> Double = { _, _, _ -> -30.0 }
        set(value) {
            field = value
            initialize(nodes)
        }

    var distanceMin
        get() = sqrt(distanceMin2)
        set(value) { distanceMin2 = value * value }

    var distanceMax
        get() = sqrt(distanceMax2)
        set(value) { distanceMax2 = value * value }

    var theta
        get() = sqrt(theta2)
        set(value) { theta2 = value * value }

    override fun initialize(nodes: List<ForceNode>) {
        this.nodes.clear()
        this.nodes.addAll(nodes)

        nodes.forEachIndexed { i, node -> node.strength =  strength(node, i, nodes) }
    }

    override fun invoke(alpha: Double) {
        val tree = Quadtree({ it.x }, { it.y }, nodes)
        tree.visitAfter(::accumulate)

        for(node in nodes) {
            tree.visit applyForce@{ quad, extents ->
                if(quad.value.isFalsy()) return@applyForce true

                var x = quad.x - node.x
                var y = quad.y - node.y
                var w = extents.width
                var l = x * x + y * y

                // Apply the Barnes-Hut approximation if possible
                if(w * w / theta2 < l) {
                    if(l < distanceMax2) {
                        if(x.isFalsy()) {
                            x = jiggle()
                            l += x * x
                        }
                        if(y.isFalsy()) {
                            y = jiggle()
                            l += y * y
                        }
                        if(l < distanceMin2) l = sqrt(distanceMin2 * l)

                        node.vx += x * quad.value * alpha / l
                        node.vy += y * quad.value * alpha / l
                    }

                    return@applyForce true
                }
                else if(quad is InternalNode<ForceNode> || l >= distanceMax2) return@applyForce false
                else if(quad is LeafNode<ForceNode>) {
                    // Limit forces for very close nodes; randomize direction if coincident
                    if(quad.data != node || quad.next != null) {
                        if(x.isFalsy()) {
                            x = jiggle()
                            l += x * x
                        }
                        if(y.isFalsy()) {
                            y = jiggle()
                            l += y * y
                        }
                        if(l < distanceMin2) l = sqrt(distanceMin2 * l)
                    }

                    var leaf: LeafNode<ForceNode>? = quad
                    do {
                        if(leaf?.data != node) {
                            w = (leaf?.data?.strength ?: 0.0) * alpha / l
                            node.vx += x * w
                            node.vy += y * w
                        }
                        leaf = leaf?.next
                    } while(leaf != null)
                }

                return@applyForce false
            }
        }
    }

    private fun accumulate(quad: QuadtreeNode<ForceNode>, extents: Rectangle2D) {
        var strength = 0.0
        var weight = 0.0

        when(quad) {
            is InternalNode<ForceNode> -> {
                var x = 0.0
                var y = 0.0

                for(q in quad.children) {
                    if(q != null && q.value.isNotNaN()) {
                        val c = abs(q.value)
                        strength += q.value
                        weight += c
                        x += c * q.x
                        y += c * q.y
                    }
                }

                quad.x = x / weight
                quad.y = y / weight
            }

            is LeafNode<ForceNode> -> {
                var q = quad as LeafNode?
                q?.x = q?.data?.x ?: 0.0
                q?.y = q?.data?.y ?: 0.0
                do {
                    strength += q?.data?.strength ?: 0.0
                    q = q?.next
                } while(q != null)
            }
        }

        quad.value = strength
    }

    private var ForceNode.strength: Double
        get() = properties["strength"] as Double? ?: 0.0
        set(value) {
            properties["strength"] = value
        }

    private var QuadtreeNode<ForceNode>.value: Double
        get() = properties["value"] as Double? ?: Double.NaN
        set(value) {
            properties["value"] = value
        }

    private var QuadtreeNode<ForceNode>.x: Double
        get() = properties["x"] as Double? ?: 0.0
        set(value) {
            properties["x"] = value
        }

    private var QuadtreeNode<ForceNode>.y: Double
        get() = properties["y"] as Double? ?: 0.0
        set(value) {
            properties["y"] = value
        }
}