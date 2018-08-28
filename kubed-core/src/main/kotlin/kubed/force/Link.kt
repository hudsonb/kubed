package kubed.force

import kubed.util.isFalsy
import kotlin.math.sqrt

fun forceLink(init: LinkForce.() -> Unit) = LinkForce().apply(init)

data class Link(val source: ForceNode, val target: ForceNode, internal var index: Int = 0)

class LinkForce(links: List<Link> = emptyList()) : Force {
    private val nodes = ArrayList<ForceNode>()
    private val links = ArrayList<Link>()
    private lateinit var distances: DoubleArray
    private lateinit var strengths: DoubleArray
    private lateinit var bias: DoubleArray

    var iterations = 1

    var distance: (link: Link, index: Int, links: List<Link>) -> Double = { _, _, _ -> 30.0 }
        set(value) {
            field = value
            initialize(nodes)
        }

    var strength: (link: Link, index: Int, links: List<Link>) -> Double = { _, _, _ -> 1.0 }
        set(value) {
            field = value
            initialize(nodes)
        }

    init {
        this.links.addAll(links)
    }

    fun link(from: ForceNode, to: ForceNode) {
        links.add(Link(from, to))
        initialize(nodes)
    }

    fun unlink(from: ForceNode, to: ForceNode) {
        links.remove(Link(from, to))
        initialize(nodes)
    }

    fun links(links: List<Link>) {
        this.links.clear()
        this.links.addAll(links)
        initialize(nodes)
    }

    override fun initialize(nodes: List<ForceNode>) {
        this.nodes.clear()
        this.nodes.addAll(nodes)

        links.forEachIndexed { i, link ->
            link.index = i
            ++link.source.count
            ++link.target.count
        }

        bias = DoubleArray(links.size) { links[it].source.count.toDouble() / (links[it].source.count + links[it].target.count) }
        strengths = DoubleArray(links.size) { strength(links[it], it, links) }
        distances = DoubleArray(links.size) { distance(links[it], it, links) }
    }

    override fun invoke(alpha: Double) {
        repeat(iterations) {
            links.forEachIndexed { i, link ->
                val source = link.source
                val target = link.target

                var x = target.x + target.vx - source.x - source.vx
                if(x.isFalsy()) x = jiggle()

                var y = target.y + target.vy - source.y - source.vy
                if(y.isFalsy()) y = jiggle()

                var l = sqrt(x * x + y * y)
                l = (l - distances[i]) / l * alpha * strengths[i]
                x *= l
                y *= l

                var b = bias[i]
                target.vx -= x * b
                target.vy -= y * b

                b = 1 - b
                source.vx += x * b
                source.vy += y * b
            }
        }
    }

    private var ForceNode.count: Int
        get() = properties["count"] as Int? ?: 0
        set(value) {
            properties["count"] = value
        }
}