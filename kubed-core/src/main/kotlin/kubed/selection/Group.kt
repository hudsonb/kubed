package kubed.selection

import javafx.scene.Node

class Group(val parent: Node) : ArrayList<Any?>() {
     /**
     * Returns true if this [Group] contains any non-null elements.
     */
    val empty: Boolean
        get() = any { it != null }
}