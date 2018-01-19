package kubed.demo

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import kubed.selection.selectAll
import kubed.shape.circle
import kubed.timer.timer
import java.lang.Math.random


class CirclesDemo : Application() {
    override fun start(primaryStage: Stage?) {
        val root = Group()

        val width = 800.0
        val height = 400.0

        data class Data(var x: Double, var y: Double, val dx: Double, val dy: Double)

        val c = circle<Data> {
            translateX { (x), _ -> x }
            translateY { (y), _ -> y }
            radius { _, _ -> 3 * random() + random() }
            fill(Color.BLACK)
        }

        val colors = kubed.color.scheme.schemeViridis()
        val circle = root.selectAll<Data>("Circle")
                .data((0..5000).map { Data(width * random(), height * random(), random() - 0.5, random() - 0.5) })
                .enter().append { d, _, _ -> c(d) }
                        .fill { _, i, _ -> colors[i % colors.size] }

        timer {
            // Update the circle positions
            circle.translateX { d, _, _ ->
                d.x += d.dx
                if (d.x > width)
                    d.x -= width
                else if (d.x < 0)
                    d.x += width

                d.x
            }
            .translateY { d, _, _ ->
                d.y += d.dy
                if(d.y > height)
                    d.y -= height
                else if(d.y < 0)
                    d.y += height
                d.y
            }
        }

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

