package kubed.force

class X : Force {
    val nodes = ArrayList<ForceNode>()

    var x = constant(Double.NaN)
        set(value) {
            field = value
            initialize(nodes)
        }

    var strength = constant(0.1)

    private lateinit var strengths: DoubleArray
    private lateinit var xz: DoubleArray

    override fun initialize(nodes: List<ForceNode>) {
        this.nodes.clear()
        this.nodes.addAll(nodes)

        xz = DoubleArray(nodes.size) { x(nodes[it], it, nodes) }
        strengths = DoubleArray(nodes.size) { if(xz[it].isNaN()) 0.0 else strength(nodes[it], it, nodes) }
    }

    override fun invoke(alpha: Double) {
        nodes.forEachIndexed { i, node ->
            node.vx += (xz[i] - node.x) * strengths[i]
        }
    }
}