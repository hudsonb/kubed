package kubed.layout.force

data class Node(var index: Int = -1,
                var x: Double = Double.NaN,
                var y: Double = Double.NaN,
                var vx: Double = Double.NaN,
                var vy: Double = Double.NaN,
                var fx: Double? = null,
                var fy: Double? = null)

class Simulation
{
    val initialRadius = 10.0
    val initialAngle = Math.PI * (3 - Math.sqrt(5.0))

    var alpha = 1.0
    var alphaTarget = 0.0
    var alphaMin = 0.001
    var alphaDecay = 1 - Math.pow(alphaMin, 1 / 300.0)
    var velocityDecay = 0.6

    val forces = HashMap<String, Force>()

    val nodes = ArrayList<Node>()

    fun addForce(key: String, force: Force) {
        forces[key] = force
    }

    fun removeForce(key: String) {
        forces -= key
    }

    fun start() {

    }

    fun initialize() {
        for(i in nodes.indices) {
            val node = nodes[i]
            node.index = i

            if(node.x.isNaN() || node.y.isNaN()) {
                val radius = initialRadius * Math.sqrt(i.toDouble())
                val angle = i * initialAngle
                node.x = radius * Math.cos(angle)
                node.y = radius * Math.sin(angle)
            }

            if(node.vx.isNaN() || node.vy.isNaN())
            {
                node.vx = 0.0
                node.vy = 0.0
            }
        }
    }

    fun step() {
        tick()

        //fireTickEvent()
        if(alpha < alphaMin) {
            //stepper.stop()
            //fireEndEvent()
        }
    }

    fun tick() {
        alpha += (alphaTarget - alpha) * alphaDecay

        forces.values.forEach {
            it(nodes, alpha)
        }

        for(i in nodes.indices) {
            val node = nodes[i]
            if(node.fx == null) node.x += node.vx * velocityDecay
            else {
                node.x = node.fx!!
                node.vx = 0.0
            }
            if(node.fy == null) node.y += node.vy * velocityDecay
            else {
                node.y = node.fy!!
                node.vy = 0.0
            }
        }
    }

    fun find(x: Double, y: Double, radius: Double = Double.MAX_VALUE): Node? {
        var r = if(radius != Double.MAX_VALUE) radius * radius else radius

        var closest: Node? = null
        for(i in nodes.indices) {
            val node = nodes[i]
            val dx = x - node.x
            val dy = y - node.y
            val d2 = dx * dx + dy * dy
            if(d2 < r) {
                closest = node
                r = d2
            }
        }

        return closest
    }
}
