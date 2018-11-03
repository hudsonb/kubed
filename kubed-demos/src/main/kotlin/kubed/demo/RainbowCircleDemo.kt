package kubed.demo

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import kubed.ScalingPane
import kubed.array.range
import kubed.color.Hsl
import kubed.color.hsl
import kubed.selection.selectAll
import kubed.shape.*
import kotlin.math.PI

class RainbowCircleDemo: Application() {
    override fun start(primaryStage: Stage) {
        val width = 960.0
        val height = 960.0
        val outerRadius = width / 2 - 20
        val innerRadius = outerRadius - 80
        val t = 2 * PI
        val n = 500

        val root = Group()

        val arc = arc<Double> {
            startAngle { d, _ -> d }
            endAngle { d, _ -> d + t / n * 1.1 }
            fill { d, _ -> hsl(d * 360 / t, 1.0, 0.5) }

            stroke(null)
            outerRadius(outerRadius)
            innerRadius(innerRadius)
        }

       root.selectAll<Double>()
           .data(range(0.0, t,  t / n).toList())
           .enter()
           .append { d, _, _ -> arc(d) }


        val sp = ScalingPane()
        sp.contentPane = StackPane(root)
        val scene = Scene(sp)
        primaryStage.scene = scene
        primaryStage.width = width
        primaryStage.height = height
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(RainbowCircleDemo::class.java, *args)
        }
    }
}