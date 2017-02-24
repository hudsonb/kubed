package kubed.demo

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.scene.CacheHint
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.transform.Rotate
import javafx.scene.transform.Translate
import javafx.stage.Stage
import kubed.selection.selectAll
import kubed.shape.rect
import kubed.util.isTruthy

class SpiralCircleDemo: Application() {
    override fun start(primaryStage: Stage) {
        val width = 960.0
        val height = 500.0
        val squareSize = 78.0
        val circleRadius = 180.0 - squareSize / 2
        val squareCount = 200
        val speed = .08

        val root = StackPane()

        val g = Group()
        root.children += g

        val rect = rect<Unit> {
            translateX(-squareSize / 2)
            translateY(-squareSize / 2)
            width(squareSize)
            height(squareSize)
            fill(Color.WHITE)
            stroke(Color.BLACK)
            cacheHint(CacheHint.ROTATE)
        }

        val g1 = Group()
        g1.clip = Rectangle(-480.0, -350.0, 960.0, 350.0)
        g1.transforms += Translate(480.0, 250.0)
        g.children += g1

        val g2 = Group()
        g2.clip = Rectangle(-480.0, 0.0, 960.0, 250.0)
        g2.transforms += Translate(480.0, 250.0)
        g.children += g2

        val square = g.selectAll<Group>()
            .append(fun(): Node { return Group() })
            .rotate { _, i, _ -> if(i.isTruthy()) 0.0 else 180.0}
            .selectAll("rect")
            .data({ _, _, _ -> (0..squareCount).toList() })
            .enter()
            .append { _, _, _ -> rect(Unit) }
            .datum { _, i, _ -> i / squareCount.toDouble() }

        g1.children.forEach { it as Group; it.children.forEach { it as Rectangle; it.stroke = Color.RED } }

        val timer = object : AnimationTimer() {
            var startTime: Long = 0

            override fun handle(now: Long) {
                if(startTime == 0.toLong())
                    startTime = now

                val elapsed = (now - startTime) / 1000000
                square.transform { t, _, _ ->
                    listOf(Rotate(t as Double * 360), Translate(0.0, circleRadius), Rotate(t * 360 + elapsed * speed, squareSize / 2, squareSize / 2))
                }
            }
        }
        timer.start()

        val scene = Scene(root)

        primaryStage.scene = scene
        primaryStage.width = width
        primaryStage.height = height
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(SpiralCircleDemo::class.java, *args)
        }
    }
}