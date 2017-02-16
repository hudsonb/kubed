package kubed.selection

import javafx.scene.Node
import javafx.scene.layout.Pane

/**
 * Default method used by [Selection.append] for adding a [Node] to the scenegraph. Only supports a [parent] that is an
 * instance of [javafx.scene.Group] or [Pane].
 *
 * @throws [IllegalArgumentException] if [parent] is neither a [javafx.scene.Group] or a [Pane].
 */
fun addNode(parent: Node, i: Int, node: Node) {
    when(parent) {
        is javafx.scene.Group -> {
            when(i) {
                -1, parent.children.size -> parent.children += node
                else -> parent.children.add(i, node)
            }
        }
        is Pane -> when(i) {
            -1, parent.children.size -> parent.children += node
            else -> parent.children.add(i, node)
        }
        else -> IllegalArgumentException("Unsupported parent class: ${parent.javaClass.name}")
    }
}

/**
 * Default method used by [Selection.remove] for remove a [Node] from the scenegraph. Only supports a parent that is an
 * instance of [javafx.scene.Group] or [Pane].
 *
 * @throws [IllegalArgumentException] if [parent] is neither a [javafx.scene.Group] or a [Pane].
 */
fun removeNode(node: Node) {
    val parent = node.parent ?: return

    when(parent) {
        is javafx.scene.Group -> parent.children -= node
        is Pane -> parent.children -= node
        else -> IllegalArgumentException("Unsupported parent class: ${parent.javaClass.name}")
    }
}

