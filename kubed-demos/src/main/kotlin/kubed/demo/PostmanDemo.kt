package kubed.demo

import javafx.animation.*
import javafx.application.Application
import javafx.scene.CacheHint
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Shape
import javafx.stage.Stage
import kubed.scale.PointScale
import kubed.shape.circle
import kubed.transition.pathTo
import javafx.util.Duration
import kubed.selection.selectAll

class PostmanDemo: Application() {
    override fun start(primaryStage: Stage?) {
        val margin = 40.0

        val root = StackPane()
        //root.translateX = margin
        //root.translateY = margin

        val width = 800.0
        val height = 400.0

        val r = PointScale<Double>()
        r.domain((0..3).map(Int::toDouble))
        r.range(listOf(25.0, 100.0))

        val ring = circle<Double> {
            radius { d, _ -> r(d) }
            stroke(Color.ORANGERED)
            fill(Color.TRANSPARENT)
            styleClasses("ring")
        }

        val rings = ArrayList<Shape>()
        root.selectAll<Double>(".ring")
                .data(r.domain)
                .enter().append { d, _, _ ->
                    val p = ring(d)
                    rings += p
                    p
                }
                .fill { _, i, _ -> if(i == 0) Color.ORANGERED else Color.TRANSPARENT }

        val orbitor = circle<Unit> {
            radius(4.0)
            stroke(Color.ORANGERED)
            fill(Color.ORANGERED)
        }

        for(i in 1..3) {
            val o = orbitor(Unit)
            o.cacheHint = CacheHint.SPEED
            root.children += o
            o.pathTo(rings[i], duration = Duration.seconds(i.toDouble()), interpolator = Interpolator.LINEAR,
                    cycleCount = Timeline.INDEFINITE)
        }

        val scene = Scene(root)
        primaryStage!!.width = width + margin * 2
        primaryStage.height = height + margin * 2

        primaryStage.scene = scene
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(PostmanDemo::class.java, *args)
        }
    }
}