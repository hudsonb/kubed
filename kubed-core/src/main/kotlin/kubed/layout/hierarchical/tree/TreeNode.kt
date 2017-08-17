package kubed.layout.hierarchical.tree

import kubed.layout.hierarchical.Node

class TreeNode(parent: TreeNode? = null, var i: Int = 0, data: Any = Unit) : Node<TreeNode>(data) {
    var defaultA: TreeNode? = null
    var a: TreeNode? = null
    var z = 0.0 // prelim
    var m = 0.0 // mod
    var c = 0.0 // change
    var s = 0.0 // shift
    var t: TreeNode? = null // thread
    var x = 0.0
    var y = 0.0
}
