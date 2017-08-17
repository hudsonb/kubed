package kubed.layout.hierarchical

import java.util.*



@Suppress("UNCHECKED_CAST")
open class Node<T : Node<T>>(var data: Any) {
    data class Link<T : Node<T>>(val source: T?, val target: T)

    var depth = 0
    var height = 0
    var parent: T? = null
    var children: List<T>? = null
    var value = 0

    /**
     * Returns a list of ancestor nodes, starting with this node, followed by each parent up to the root.
     */
    fun ancestors(): List<T> {
        val nodes = LinkedList<T>()

        var node: T? = this as T
        while(node != null) {
            nodes.push(node)
            node = node.parent
        }

        return nodes
    }

    /**
     * Returns a list of descendant nodes, starting with this node, followed by each child in topological order.
     */
    fun descendants(): List<T> {
        val nodes = LinkedList<T>()
        forEach { nodes += it }
        return nodes
    }

    /**
     * Returns a list of leaf nodes in traversal order. Leaves are defined as nodes with no children.
     */
    fun leaves(): List<T> {
        val leaves = LinkedList<T>()
        forEachBefore { node ->
            val children = node.children
            if(children == null || children.isEmpty())
                leaves.push(node)
        }

        return leaves
    }

    /**
     * Returns the shortest path through the hierarchy from this node to the specified node. The path starts at this
     * node, ascends to teh least common ancestor of this node and the specified node, then descends to the target node.
     */
    fun path(to: T): List<T> {
        var from: T? = this as T
        val ancestor = leastCommonAncestor(from!!, to)
        val nodes = LinkedList<T>(listOf(from))

        while(from != ancestor) {
            from = from?.parent
            nodes.push(from)
        }

        val k = nodes.size
        var end: T? = to
        while(end != ancestor) {
            if(nodes.size <= k) nodes.add(end!!)
            else nodes.add(k, end!!)

            end = end.parent
        }

        return nodes
    }

    fun links(): List<Link<T>> {
        val root = this
        val links = LinkedList<Link<T>>()

        root.forEach { node ->
            if(node != root) { // Don't include the root's parent, if any.
                links.push(Link(node.parent, node))
            }
        }

        return links
    }

    fun sum(value: (data: Any) -> Int = { 0 }): T {
        forEachAfter { node ->
            var sum = value(node.data)
            val children = node.children
            if(children != null && children.isNotEmpty()) {
                var i = children.size
                while(--i >= 0) sum += children[i].value
            }
            node.value = sum
        }

        return this as T
    }

    /**
     * Calculates the number of leaves under this node and assigns it to [value], and similarly for every descendant. IF
     * this node is a lead, its count is one.
     *
     * @return This node.
     */
    fun count(): T {
        forEachAfter(this::countNode)
        return this as T
    }

    private fun countNode(node: T) {
        var sum = 0
        val children = node.children
        if(children == null || children.isEmpty()) sum = 1
        else
        {
            var i = children.size
            while(--i >= 0) sum += children[i].value
        }

        node.value = sum
    }

    fun sort(comparator: Comparator<T>) {
        forEachBefore { node ->
            node.children = node.children?.sortedWith(comparator)
        }
    }

    fun forEach(callback: (T) -> Unit) {
        val next = LinkedList<T>()
        next += this as T

        val current = LinkedList<T>()

        do {
            current.clear()
            current.addAll(next.reversed())
            next.clear()

            for(i in current.indices) {
                val node = current[i]
                callback(node)
                val children = node.children
                if(children != null) {
                    for(n in children)
                        next.push(n)
                }
            }
        } while(next.isNotEmpty())
    }

    fun forEachAfter(callback: (T) -> Unit) {
        val nodes = LinkedList<T>()
        nodes += this as T

        val next = LinkedList<T>()

        while(nodes.isNotEmpty()) {
            val node = nodes.pop()
            next.push(node)

            val children = node.children
            if(children != null) {
                for(n in children)
                    nodes.push(n)
            }
        }

        for(n in next)
            callback(n)
    }

    fun forEachBefore(callback: (T) -> Unit) {
        val nodes = LinkedList<T>()
        nodes += this as T

        while(nodes.isNotEmpty()) {
            val node = nodes.pop()
            callback(node)

            val children = node.children
            if(children != null) {
                for(n in children)
                    nodes.push(n)
            }
        }
    }

    private fun leastCommonAncestor(a: T, b: T): T? {
        if(a == b) return a

        val aNodes = LinkedList(a.ancestors())
        val bNodes = LinkedList(b.ancestors())

        var a2 = if(aNodes.isNotEmpty()) aNodes.pop() else null
        var b2 = if(bNodes.isNotEmpty()) bNodes.pop() else null

        var c: T? = null
        while(a2 == b2) {
            c = a2
            a2 = if(aNodes.isNotEmpty()) aNodes.pop() else null
            b2 = if(bNodes.isNotEmpty()) bNodes.pop() else null
        }

        return c
    }
}
