package kubed.demo

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import kubed.selection.selectAll
import kubed.shape.circle


class CirclesDemo : Application() {
    override fun start(primaryStage: Stage?) {
        val root = Group()

        val width = 800.0
        val height = 400.0

        data class Data(var x: Double, var y: Double, val dx: Double, val dy: Double)

        val c = circle<Data> {
            translateX(Data::x)
            translateY(Data::y)
            radius { 3 * Math.random() + Math.random() }
            fill(Color.BLACK)
        }

        val circle = root.selectAll<Node>()
                .data(listOf((0..5000).map { Data(width * Math.random(), height * Math.random(), Math.random() - 0.5, Math.random() - 0.5) }))
                .enter().append { d, _, _ -> c(d as Data) }

        val timer = object : AnimationTimer() {
            override fun handle(now: Long) {
                // Update the circle positions.
                circle.translateX { d, _, _ ->
                    d as Data
                    d.x += d.dx
                    if (d.x > width)
                        d.x -= width
                    else if (d.x < 0)
                        d.x += width

                    d.x
                }
                .translateY { d, _, _ ->
                    d as Data
                    d.y += d.dy
                    if(d.y > height)
                        d.y -= height
                    else if(d.y < 0)
                        d.y += height
                    d.y
                }
            }
        }
        timer.start()


        val scene = Scene(root)
        primaryStage?.width = width
        primaryStage?.height = height

        primaryStage?.scene = scene
        primaryStage?.show()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(CirclesDemo::class.java, *args)
        }
    }
}

