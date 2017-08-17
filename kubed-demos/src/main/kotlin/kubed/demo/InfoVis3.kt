package kubed.demo

import javafx.application.Application
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Shape
import javafx.stage.Stage
import kubed.scale.scaleBand
import kubed.scale.scaleLinear
import kubed.selection.selectAll
import kubed.shape.rect

class InfoVis3 : Application() {
    override fun start(primaryStage: Stage?) {
        val width = 150.0
        val height = 50.0

        val root = Group()
        root.prefWidth(width)
        root.prefHeight(height)

        val data = listOf(Point2D(5.0, .8 * height),
                Point2D(12.0, .7 * height),
                Point2D(19.0, .5 * height),
                Point2D(26.0, .4 * height),
                Point2D(33.0, .3 * height),
                Point2D(40.0, .9 * height),
                Point2D(47.0, .6 * height),
                Point2D(54.0, .8 * height),
                Point2D(61.0, .5 * height),
                Point2D(68.0, .4 * height),
                Point2D(75.0, .4 * height),
                Point2D(82.0, .7 * height),
                Point2D(89.0, .5 * height),
                Point2D(89.0, .6 * height),
                Point2D(96.0, .8 * height))

        val x = scaleBand<Int> {
            rangeRound(listOf(0.0, width))
            domain(data.indices.toList())
            padding(0.1)
        }

        val y = scaleLinear<Double> {
            range(listOf(height, 0.0))
            domain(listOf(0.0, data.map { it.y }.max()!!))
        }

        val bar = rect<Point2D> {
            width(x.bandwidth)
            height { d, _ -> height - y(d.y) }
            fill(Color.STEELBLUE)
        }

        root.selectAll<Point2D>(".bar")
            .data(data)
            .enter()
            .append { d, _, _ -> bar(d) }
            .translateX { _, i, _ -> x(i) }
            .translateY { d, _, _ -> y(d.y) }
            .on(MouseEvent.MOUSE_ENTERED) { (this as Shape).fill = Color.RED }
            .on(MouseEvent.MOUSE_EXITED) { (this as Shape).fill = Color.STEELBLUE }

        val scene = Scene(root)
        primaryStage?.scene = scene
        primaryStage?.show()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(InfoVis3::class.java, *args)
        }
    }
}