package kubed.demo

import javafx.application.Application
import javafx.application.Application.launch
import javafx.scene.*
import javafx.scene.paint.Color
import javafx.scene.paint.Color.color
import javafx.scene.shape.Rectangle
import javafx.scene.transform.Rotate
import javafx.scene.transform.Translate
import javafx.stage.Stage
import kubed.color.Cubehelix
import kubed.color.cubehelix
import kubed.interpolate.color.interpolateCubeHelix
import kubed.scale.LinearScale
import kubed.scale.scaleLinear
import kubed.selection.selectAll
import kubed.shape.rect
import kubed.timer.timer
import kotlin.math.sqrt

class SpiralTriangleDemo: Application() {
    override fun start(primaryStage: Stage) {
        val width = 960.0
        val height = 500.0
        val squareSize = 90.0
        val triangleSize = 400.0
        val squareCount = 71
        val angularSpeed = .08
        val colorSpeed = angularSpeed / 240

        val root = Group()

        val g = Group()
        root.children += g

        val color = scaleLinear(::interpolateCubeHelix) {
                domain(0.0, 0.5, 1.0)
                range(Cubehelix(-100.0, 0.75, 0.35).toColor(),
                      Cubehelix(80.0, 1.0, 0.80).toColor(),
                      Cubehelix(260.0, 0.75, 0.35).toColor())
        }

        val rect = rect<Unit> {
            translateX(-squareSize / 2)
            translateY(-squareSize / 2)
            width(squareSize)
            height(squareSize)
            fill(Color.WHITE)
            stroke(Color.BLACK)
            cache(true)
            cacheHint(CacheHint.ROTATE)
        }

        val g1 = Group()
        g1.clip = Rectangle(-480.0, 0.0, 960.0, 195.0)
        g1.translateX = 480.0
        g1.translateY = 305.0
        g.children += g1

        val g2 = Group()
        g2.clip = Rectangle(-480.0, -305.0, 960.0, 305.0)
        g2.translateX = 480.0
        g2.translateY = 305.0
        g.children += g2

        val square = g.selectAll<Unit>("Group")
                      .selectAll<Int>("Group")
                      .data<Unit>({ _, i, _ -> if(i == 0) listOf(0, 1, 2) else listOf(2, 0, 1) })
                      .enter()
                .append { _, _, _ -> Group() }
                      //.append(fun(): Node { return Group() })
                      .transform { d, _, _ -> listOf(Rotate(d * 120.0 + 60.0), Translate(0.0, -triangleSize / sqrt(12.0))) }
                      .selectAll<Double>("Rectangle")
                      .data<Int>({ _, _, _ -> (0..squareCount).map(Int::toDouble) })
                      .enter()
                      .append { _, _, _ -> rect(Unit) }
                      .datum { _, i, _ -> i / squareCount.toDouble() }

        timer { elapsed ->
            square.fill { t, _, _ -> color((t + elapsed * colorSpeed) % 1) }
            square.transform { t, _, _ ->
                listOf(Translate((t - .5) * triangleSize, 0.0), Rotate(t * 120 + elapsed * angularSpeed, squareSize / 2, squareSize / 2))
            }
        }

        val scene = Scene(root)

        primaryStage.scene = scene
        primaryStage.width = width
        primaryStage.height = height
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(SpiralTriangleDemo::class.java, *args)
        }
    }
}