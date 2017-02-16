package kubed.selection

import javafx.scene.Node
import javafx.scene.Parent
import java.util.*
import kotlin.comparisons.compareBy

/**
 * Selects the first node that matches the specified selector. If no nodes match the selector, returns an emptySelection selection.
 * If multiple elements match the selector only the first matching element (in scene graph order) will be selected.
 */
fun Node.select(selector: String): Selection {
    val sel = Selection()
    val group = Group(this)
    sel += group

    var node: Node? = lookup(selector)

    // lookup can return the node itself, which is undesirable
    if(node == this && this is Parent) {
        // Consider: Is there a faster way to lookup the first matching sub-node?
        val nodes = node.lookupAll(selector)
        nodes -= this
        node = nodes.sortedWith(compareBy(Node::depth, Node::pos)).firstOrNull()
    }

    if(node != null)
        group += node

    return sel
}

/**
 *
 */
fun Node.select(node: Node): Selection {
    // Consider: Should we support this? What if the Node has not been added to the Scene or if it is not a descendant
    //           of this node?
    TODO("Not yet implemented")
}

fun Node.emptySelection(): Selection {
    val sel = Selection()
    val group = Group(this)
    sel.plusAssign(group)
    return sel
}

inline fun <reified T : Node> Node.selectAll(): Selection {
    val sel = Selection()
    val group = Group(this)
    sel.plusAssign(group)
    group.addAll(lookupChildrenOfType<T>())
    return sel
}

fun Node.selectAll(selector: String): Selection {
    val sel = Selection()
    val group = Group(this)
    sel += group

    val nodes = lookupAll(selector)
    group.addAll(nodes.sortedWith(compareBy(Node::depth, Node::pos)))

    return sel
}

internal fun Node.lookupChildren(selector: String): Node? {
    var node: Node? = lookup(selector)

    // lookup can return the node itself, which is undesirable
    if(node == this && this is Parent) {
        // Consider: Is there a faster way to lookup the first matching sub-node?
        val nodes = node.lookupAll(selector)
        nodes -= this
        node = nodes.sortedWith(compareBy(Node::depth, Node::pos)).firstOrNull()
    }

    return node
}

/**
 *
 */
internal fun Node.lookupAllChildren(selector: String): Set<Node> {
    val set = HashSet<Node>()
    set += lookupAll(selector)
    set -= this
    return set
}

/**
 * The datum associated with this Node, or Selection.UNDEFINED if there is none.
 */
var Node.datum: Any?
    get() = properties[Selection.DATA_PROPERTY]
    set(value) { properties[Selection.DATA_PROPERTY] = value }

/**
 * The position of the node within it's parent list of children, or -1 if it is orphaned.
 */
internal val Node.pos: Int
    get() = parent?.childrenUnmodifiable?.indexOf(this) ?: -1

/**
 * The depth of the node in the scene graph, or -1 if it is orphaned.
 */
internal val Node.depth: Int
    get() {
        var depth = -1

        var node = this
        while (node.parent != null) {
            ++depth
            node = node.parent
        }

        return depth
    }

/**
 * Finds all descendants of type [T].
 *
 * The nodes in the returned list are in scenegraph order.
 */
inline fun <reified T> Node.lookupChildrenOfType(): List<Node> {
    val children = ArrayList<Node>()

    if(this is Parent) {
        val stack = LinkedList<Node>()
        stack.push(this)

        while(stack.isNotEmpty()) {
            val node = stack.pop()
            if(node is T)
                children += node

            if(node is Parent)
                node.childrenUnmodifiable.forEach { stack.push(it) }
        }

        children -= this
    }

    return children
}