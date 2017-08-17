package kubed.layout.hierarchical.tree

import javafx.geometry.Dimension2D
import kubed.util.isTruthy
import java.util.*

class TidyTree {
    var separation = { a: TreeNode, b: TreeNode -> if(a.parent == b.parent) 1 else 2 }
    private var nodeSize = false
    private var dx = 1.0
    private var dy = 1.0

    fun size(x: Dimension2D): TidyTree {
        nodeSize = false
        dx = x.width
        dy = x.height

        return this
    }

    fun size(): Dimension2D? {
        return when(nodeSize) {
            true -> null
            else -> Dimension2D(dx, dy)
        }
    }

    fun nodeSize(x: Dimension2D): TidyTree {
        nodeSize = true
        dx = x.width
        dy = x.height

        return this
    }

    fun nodeSize(): Dimension2D? {
        return when(nodeSize) {
            true -> Dimension2D(dx, dy)
            else -> null
        }
    }

    operator fun invoke(root: TreeNode): TreeNode {
        val t = treeRoot(root)

        // Compute the layout using Buchheim et al.'s algorithm
        t.forEachAfter(this::firstWalk)
        t.parent?.m = -t.z
        t.forEachBefore(this::secondWalk)

        // If a fixed node sie is specified, scale x and y.
        if(nodeSize) root.forEachBefore(this::sizeNode)
        else {
            // If a fixed tree size is specified, scale x and y based on the extend.
            // Compute the left-mot, right-most, and depth-most nodes for extents.
            var left = root
            var right = root
            var bottom = root

            root.forEachBefore { node ->
                if(node.x < left.x) left = node
                if(node.x > right.x) right = node
                if(node.depth > bottom.depth) bottom = node
            }

            val s = if(left == right) 1 else separation(left, right) / 2
            val tx = s - left.x
            val kx = dx / (right.x + s + tx)
            val ky = dy / (if(bottom.depth != 0) bottom.depth else 1)
            root.forEachBefore { node ->
                node.x = (node.x + tx) * kx
                node.y = node.depth * ky
            }
        }

        return root
    }

    private fun treeRoot(root: TreeNode): TreeNode {
        val tree = TreeNode(root, 0)
        val nodes = LinkedList<TreeNode>()

        var children: List<TreeNode>?
        var node: TreeNode? = tree
        while(node != null) {
            children = node.children
            if(children != null && children.isNotEmpty()) {
                for(i in children.indices.reversed()) {
                    val child = TreeNode(children[i], i)
                    child.parent = node


                    nodes.push(child)
                }
            }

            node = if(nodes.isNotEmpty()) nodes.pop() else null
        }

        tree.parent = TreeNode(null, 0).apply { children = arrayListOf(tree) }

        return tree
    }

    private fun nextLeft(v: TreeNode) = v.children?.first() ?: v.t
    private fun nextRight(v: TreeNode) = v.children?.last() ?: v.t
    private fun nextAncestor(vim: TreeNode, v: TreeNode, ancestor: TreeNode) = if(vim.a?.parent == v.parent) vim.a else ancestor

    private fun firstWalk(v: TreeNode) {
        val siblings = v.parent?.children ?: emptyList<TreeNode>()
        val w = if(v.i.isTruthy()) siblings[v.i - 1] else null

        val children = v.children
        if(children != null && children.isNotEmpty())
        {
            executeShifts(v)
            val midpoint = (children.first().z + children.last().z) / 2.0
            if(w != null) {
                v.z = w.z + separation(v, w)
                v.m = v.z - midpoint
            }
            else v.z = midpoint
        }
        else if(w != null) {
            v.z = w.z + separation(v, w)
        }

        v.parent?.defaultA = apportion(v, w, v.parent?.defaultA ?: siblings.first())
    }

    private fun secondWalk(v: TreeNode) {
        v.x = v.z + (v.parent?.m ?: 0.0)
        v.m += v.parent?.m ?: 0.0
    }

    private fun moveSubtree(wm: TreeNode, wp: TreeNode, shift: Double) {
        val change = shift / (wp.i - wm.i)
        wp.c -= change
        wp.s += shift
        wm.c += change
        wp.z += shift
        wp.m += shift
    }

    /**
     * All other shifts, applied to the smaller subtrees between w- and w+, are performed by this function. To prepare
     * the shifts, we have to adjust change(w+), shift(w+) and change(w-).
     */
    private fun executeShifts(v: TreeNode) {
        var shift = 0.0
        var change = 0.0
        var w: TreeNode?

        val children = v.children
        if(children != null) {
            var i = children.size
            while (--i >= 0) {
                w = children[i]
                w.z += shift
                w.m += shift
                change += w.c
                shift += w.s + change
            }
        }
    }

    private fun apportion(v: TreeNode, w: TreeNode?, ancestor: TreeNode): TreeNode
    {
        var a = ancestor
        if(w != null) {
            var vip: TreeNode? = nextLeft(v)
            var vop: TreeNode? = v
            var vim: TreeNode? = nextRight(w)
            var vom = vip?.parent?.children?.first()
            var sip = vip?.m ?: 0.0
            var sop = vop?.m ?: 0.0
            var sim = vim?.m ?: 0.0
            val som = vom?.m ?: 0.0
            var shift: Double

            while(vim != null && vip != null) {
                vom = nextLeft(vom!!)
                vop = nextRight(vop!!)
                vop?.a = v
                shift = vim.z + sim - vip.z - sip + separation(vim, vip)
                if(shift > 0) {
                    moveSubtree(nextAncestor(vim, v, ancestor)!!, v, shift)
                    sip += shift
                    sop += shift
                }
                sim += vim.m
                sip += vip.m
                sim += vom?.m ?: 0.0
                sop += vop?.m ?: 0.0

                vim = nextRight(vim)
                vip = nextLeft(vip)
            }

            if(vim != null && (vop == null || nextRight(vop) == null)) {
                if(vop != null) {
                    vop.t = vim
                    vop.m += sim - sop
                }
            }

            if(vip != null && (vom == null || nextLeft(vom) == null)) {
                if(vom != null) {
                    vom.t = vip
                    vom.m += sip - som
                    a = v
                }
            }
        }

        return a
    }

    private fun sizeNode(node: TreeNode) {
        node.x *= dx
        node.y = node.depth * dy
    }
}