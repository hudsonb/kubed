package kubed.transition

import javafx.animation.Transition
import javafx.scene.CacheHint
import javafx.scene.Node

class CacheHintTransition(val node: Node, transition: Transition, val cacheHint: CacheHint) : Transition() {
    init {
        val originalCacheHint = node.cacheHint
        transition.statusProperty().addListener { _, _, status ->
            when(status) {
                Status.RUNNING -> node.cacheHint = cacheHint
                Status.STOPPED, Status.PAUSED, null -> node.cacheHint = originalCacheHint
            }
        }
    }

    override fun interpolate(frac: Double) {}
}