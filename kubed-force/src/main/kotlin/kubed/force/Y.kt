package kubed.force

class Y : Force {
    val nodes = ArrayList<ForceNode>()

    var y = constant(Double.NaN)
        set(value) {
            field = value
            initialize(nodes)
        }

    var strength = constant(0.1)

    private lateinit var strengths: DoubleArray
    private lateinit var yz: DoubleArray

    override fun initialize(nodes: List<ForceNode>) {
        this.nodes.clear()
        this.nodes.addAll(nodes)

        yz = DoubleArray(nodes.size) { y(nodes[it], it, nodes) }
        strengths = DoubleArray(nodes.size) { if(yz[it].isNaN()) 0.0 else strength(nodes[it], it, nodes) }
    }

    override fun invoke(alpha: Double) {
        nodes.forEachIndexed { i, node ->
            node.vy += (yz[i] - node.x) * strengths[i]
        }
    }
}