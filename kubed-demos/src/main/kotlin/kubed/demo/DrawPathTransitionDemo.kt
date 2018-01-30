package kubed.demo

import javafx.animation.ParallelTransition
import javafx.animation.PathTransition
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Path
import javafx.stage.Stage
import javafx.util.Duration
import kubed.animation.DrawPathTransition
import kubed.interpolate.interpolateRound
import kubed.scale.scaleLinear
import kubed.shape.curve.*
import kubed.shape.line
import java.lang.Math.random

class DrawPathTransitionDemo : Application() {
    override fun start(primaryStage: Stage?) {
        val width = 700.0
        val height = 300.0

        val root = Group()
        root.prefWidth(width)
        root.prefHeight(height)

        val data = (1..10).map { random() * 10 }

        val xScale = scaleLinear(::interpolateRound) {
            range(0.0, width)
            domain(0.0, 10.0)
        }

        val yScale = scaleLinear(::interpolateRound) {
            range(10.0, height - 10.0)
            domain(0.0, 10.0)
        }

        val line = line<Double> {
            x { _, i, _ -> xScale(i.toDouble()) }
            y { d, _, _ -> yScale(d) }
            curve(curveCardinal())
            stroke(Color.STEELBLUE)
            strokeWidth(2.0)
        }

        val a = line(data)
        a.stroke = Color.LIGHTGRAY
        a.strokeWidth = 0.5

        val b = line(data)
        b.strokeDashArray.addAll(3.0, 5.0)

        val c = Circle(5.0)
        c.fill = Color.BLACK

        val pt = PathTransition(Duration.seconds(10.0), a, c)

        root.children += a
        root.children += b
        root.children += c

        val dt = DrawPathTransition(b as Path)
        dt.duration = Duration.seconds(10.0)

        ParallelTransition(pt, dt).play()

        val scene = Scene(root)
        primaryStage?.scene = scene
        primaryStage?.show()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(DrawPathTransitionDemo::class.java, *args)
        }
    }
}