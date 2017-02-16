package kubed.demo

import apple.laf.JRSUIState
import javafx.animation.Animation
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.util.Duration
import kubed.scale.PointScale
import kubed.selection.selectAll
import kubed.shape.circle
import kubed.transition.*


class DiscoModeDemo: Application() {
    override fun start(primaryStage: Stage?) {
        val margin = 40.0

        val root = Group()
        root.translateX = margin
        root.translateY = margin

        val width = 800.0
        val height = 400.0

        val x = PointScale<Double>()
        x.domain((0..3).map(Int::toDouble))
        x.range(listOf(0.0, height))

        val c = circle<Double>().radius(25.0)
                .translateX(x::invoke)
                .stroke(Color.BLACK)
                .fill(Color.GREEN)
        root.selectAll<Node>()
                .data(listOf(x.domain))
                .enter().append { d, _, _ -> c(d as Double) }
                .bind({ _, _, _ -> translateYProperty() }, primaryStage!!.heightProperty().divide(2).subtract(margin + 25.0 / 2))
                .transition()
                .delay { _, i, _ -> Duration.millis(i * 50.0) }
                .duration(Duration.seconds(1.0))
                .on(Animation.Status.RUNNING, { repeat(this) })

        val scene = Scene(root)
        primaryStage.width = width + margin * 2
        primaryStage.height = height + margin * 2

        primaryStage.scene = scene
        primaryStage.show()
    }

    fun repeat(node: Node) {
        println("r");
        node.active()?.fill(Color.RED)
                     ?.transition()
                     ?.duration(Duration.seconds(1.0))
                     ?.fill(Color.GREEN)
                     ?.transition()
                     ?.duration(Duration.seconds(1.0))
                     ?.fill(Color.BLUE)
                     ?.transition()
                     ?.on(Animation.Status.RUNNING) { repeat(this) }
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(DiscoModeDemo::class.java, *args)
        }
    }
}