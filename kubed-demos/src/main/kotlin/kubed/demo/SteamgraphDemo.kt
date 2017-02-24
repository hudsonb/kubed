package kubed.demo

import javafx.application.Application
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import kubed.interpolate.interpolateNumber
import kubed.scale.LinearScale
import kubed.selection.selectAll
import kubed.shape.*

class SteamgraphDemo: Application() {
    override fun start(primaryStage: Stage?) {
        val width = 960.0
        val height = 500.0

        val root = Group()
        root.prefWidth(width)
        root.prefHeight(height)

        val n = 2 // Number of layers
        val m = 200 // Number of samples per layer
        val test = (0 until n).map { bumpLayer(m) }
        val layers = stack<List<Point2D>, Int>({ (0 until m).map { it }}, { d, k -> d[k].y }, test)

        val xScale = LinearScale<Double>(::interpolateNumber).domain(listOf(0.0, (m - 1).toDouble()))
                                                        .range(listOf(0.0, width))

        val max = layers.map { it.map { it.y0 + it.y1 } }.flatMap { it }.max() ?: throw IllegalArgumentException()
        val yScale = LinearScale(::interpolateNumber).domain(listOf(0.0, max))
                                                .range(listOf(height, 0.0))

//        val area = area<Point<Point2D>> {
//            x { d, i, _ -> xScale(i.toDouble()) }
//            y0 { d, i, _ -> yScale(d.y0) }
//            y1 { d, i, _ -> yScale(d.y0 + d.y1) }
//            fill(Color.ORANGERED)
//        }
//
//        root.selectAll("path")
//            .data(listOf(layers))
//            .enter()
//            .append { d, _, _ -> area(d as List<Point<Point2D>>) }

        val scene = Scene(root)
        primaryStage?.scene = scene
        primaryStage?.show()
    }

    fun bumpLayer(n: Int): List<Point2D> {
        val a = DoubleArray(n, { 0.0 })
        (0 until 5).forEach {
            bump(a)
        }

        var i = 0.0
        return a.map { Point2D(i++, Math.max(0.0, it)) }
    }

    fun bump(a: DoubleArray) {
        val x = 1.0 / (0.1 + Math.random())
        val y = 2.0 * Math.random() - 0.5
        val z = 10.0 / (0.1 + Math.random())

        for(i in a.indices) {
            val w = (i / a.size.toDouble() - y) * z
            a[i] += x * Math.exp(-w * w)
        }
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(SteamgraphDemo::class.java, *args)
        }
    }
}