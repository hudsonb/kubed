package kubed.force

import javafx.geometry.Point2D

fun forceCenter(x: Double = 0.0, y: Double = 0.0) = forceCenter(x, y) {}
fun forceCenter(x: Double = 0.0, y: Double = 0.0, init: Center.() -> Unit) = forceCenter(Point2D(x, y), init)
fun forceCenter(center: Point2D = Point2D(0.0, 0.0)) = forceCenter(center) {}
fun forceCenter(center: Point2D = Point2D(0.0, 0.0), init: Center.() -> Unit) = Center(center).apply(init)

class Center(val center: Point2D = Point2D(0.0, 0.0)) : Force {
    private val nodes = ArrayList<ForceNode>()

    override fun initialize(nodes: List<ForceNode>) {
        this.nodes.addAll(nodes)
    }

    override fun invoke(alpha: Double) {
        var sx = 0.0
        var sy = 0.0

        nodes.forEach { node ->
            sx += node.x
            sy += node.y
        }

        sx = sx / nodes.size - center.x
        sy = sy / nodes.size - center.y

        nodes.forEach { node ->
            node.x -= sx
            node.y -= sy
        }
    }
}