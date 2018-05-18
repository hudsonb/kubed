package kubed.demo

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.effect.BlendMode
import javafx.scene.paint.Color
import javafx.scene.shape.Path
import javafx.scene.shape.StrokeLineJoin
import javafx.stage.Stage
import kubed.array.range
import kubed.selection.selectAll
import kubed.shape.RadialLine
import kubed.shape.curve.curveLinearClosed
import kubed.shape.radialLine
import kubed.timer.timer
import java.lang.Math.pow
import kotlin.math.PI
import kotlin.math.cos

class CircleWaveDemo : Application() {
    override fun start(primaryStage: Stage?) {
        val width = 960.0
        val height = 500.0

        val root = Group()
        root.translateX = width / 2
        root.translateY = height / 2

        val angles = range(0.0, 2 * PI, PI / 200).asList()

        root.selectAll<Color>()
            .data(listOf(Color.CYAN, Color.MAGENTA, Color.YELLOW))
            .enter()
            .append { d, _, _ ->
                Path().apply {
                    stroke = d
                    strokeWidth = 10.0
                    strokeLineJoin = StrokeLineJoin.ROUND
                    blendMode = BlendMode.DARKEN
                }
            }

        val path = root.selectAll<RadialLine<Double>>("Path")
            .datum { _, i, _ ->
                radialLine<Double>().curve(curveLinearClosed())
                                    .angle { a, _, _ ->  a }
                                    .radius { a, _, _ ->
                                        val t = System.currentTimeMillis() / 1000.0
                                        200 + cos(a * 8 - i * 2 * PI / 3 + t) * pow((1 + cos(a - t)) / 2, 3.0) * 32
                                    }
            }


        timer {
            path.forEach<Path> { d, _, _ -> elements.setAll((d(angles) as Path).elements) }
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
            launch(CircleWaveDemo::class.java, *args)
        }
    }
}

