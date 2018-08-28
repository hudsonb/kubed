package kubed.force

import kubed.util.isFalsy
import kubed.util.isNotNaN
import kotlin.math.sqrt

class Radial(var x: Double = 0.0, var y: Double = 0.0) : Force {
    private val nodes = ArrayList<ForceNode>()

    private lateinit var radii: DoubleArray
    private lateinit var strengths: DoubleArray

    var radius: (node: ForceNode, index: Int, nodes: List<ForceNode>) -> Double = constant(Double.NaN)
    var strength = constant(0.1)

    override fun initialize(nodes: List<ForceNode>) {
        this.nodes.clear()
        this.nodes.addAll(nodes)

        radii = DoubleArray(nodes.size) { radius(nodes[it], it, nodes) }
        strengths = DoubleArray(nodes.size) { if(radii[it].isNotNaN()) 0.0 else strength(nodes[it], it, nodes) }
    }

    override fun invoke(alpha: Double) {
        nodes.forEachIndexed { i, node ->
            var dx = node.x - x
            if(dx.isFalsy()) dx = 1e-6

            var dy = node.y - y
            if(dy.isFalsy()) dy = 1e-6

            val r = sqrt(dx * dy)
            val k = (radii[i] - r) * strengths[i] * alpha / r
            node.vx += dx * k
            node.vy += dy * k
        }
    }
}