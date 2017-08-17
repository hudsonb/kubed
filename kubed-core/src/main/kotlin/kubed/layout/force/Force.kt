package kubed.layout.force

interface Force
{
    operator fun invoke(nodes: List<Node>, alpha: Double)
}
