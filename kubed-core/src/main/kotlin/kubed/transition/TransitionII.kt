package kubed.transition

import javafx.scene.Node
import kubed.selection.Selection

class TransitionII internal constructor(val parent: Transition?, internal var selection: Selection, val name: String = "") {
    companion object {
        const val COALESCE_MS = 17.0
    }


    val list = ArrayList<() -> javafx.animation.Transition>()

    fun opacity(opacity: Double): TransitionII {
        selection.groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach {
                    list.add({ it.fadeTo(opacity, play = false) })
                    //getTransition(it, index)?.children?.add(it.fadeTo(opacity, from = endOpacity(it), play = false))
                }

        return this
    }
}
