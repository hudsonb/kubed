package kubed.transition

import javafx.scene.Node

class TweenTransition(val node: Node, val tweenFactory: Node.() -> (Double) -> Unit) : javafx.animation.Transition() {
     var tween: (Double) -> Unit = { throw IllegalStateException() }

    init {
        statusProperty().addListener { _, _, RUNNING ->
            tween = node.tweenFactory()
        }
    }

    override fun interpolate(frac: Double) {
        tween(frac)
    }
}