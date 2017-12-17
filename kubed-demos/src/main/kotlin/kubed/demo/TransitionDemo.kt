package kubed.demo

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.util.Duration
import kubed.scale.PointScale
import kubed.selection.selectAll
import kubed.shape.circle
import kubed.transition.*


class TransitionDemo: Application() {
    override fun start(primaryStage: Stage) {
        val margin = 40.0

        val root = Group()
        root.translateX = margin
        root.translateY = margin

        val width = 800.0
        val height = 400.0

        val x = PointScale<Double>()
        x.domain((0..3).map(Int::toDouble))
        x.range(listOf(0.0, height))

        val c = circle<Double> {
            radius(25.0)
            translateX { d, _ -> x(d) }
            translateYProperty(primaryStage.heightProperty().divide(2).subtract(margin + 25.0 / 2))
            stroke(Color.BLACK)
            fill(Color.GREEN)
        }

        root.selectAll<Double>()
                .data(x.domain)
                .enter().append { d, _, _ -> c(d) }
                .bind({ _, _, _ -> translateYProperty() }, primaryStage.heightProperty().divide(2).subtract(margin + 25.0 / 2))
                .transition()
                .duration(Duration.millis(1000.0))
                .fill(Color.YELLOW)
                .transition()
                .fill(Color.RED)

        val scene = Scene(root)
        primaryStage.width = width + margin * 2
        primaryStage.height = height + margin * 2

        primaryStage.scene = scene
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(TransitionDemo::class.java, *args)
        }
    }
}

