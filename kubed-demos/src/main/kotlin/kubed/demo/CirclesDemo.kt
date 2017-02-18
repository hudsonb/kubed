package kubed.demo

import javafx.animation.AnimationTimer
import javafx.animation.Timeline
import javafx.application.Application
import javafx.application.Application.launch
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.util.Duration
import jdk.nashorn.internal.objects.NativeFunction.function
import kubed.color.Hsl
import kubed.color.hcl
import kubed.interpolate.interpolateHcl
import kubed.scale.LinearScale
import kubed.scale.PointScale
import kubed.selection.selectAll
import kubed.shape.circle
import kubed.transition.transition
import javax.management.Query.attr
import kotlin.coroutines.experimental.EmptyCoroutineContext.plus


class CirclesDemo : Application() {
    override fun start(primaryStage: Stage?) {
        val root = Group()

        val width = 800.0
        val height = 400.0

        data class Data(var x: Double, var y: Double, val dx: Double, val dy: Double)

        val c = circle<Data> {
            translateX(Data::x)
            translateY(Data::y)
            radius(2.5)
            fill(Color.BLACK)
        }

        val circle = root.selectAll<Node>()
                .data(listOf((0..1000).map { Data(width * Math.random(), height * Math.random(), Math.random() - 0.5, Math.random() - 0.5) }))
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

