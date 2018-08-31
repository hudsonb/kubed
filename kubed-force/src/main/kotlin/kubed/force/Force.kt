package kubed.force

fun constant(value: Double) = { _: ForceNode, _: Int, _: List<ForceNode> -> value }

interface Force {
    fun initialize(nodes: List<ForceNode>)
    operator fun invoke(alpha: Double)
}