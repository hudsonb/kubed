package kubed.demo

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.stage.Stage
import kubed.selection.emptySelection
import kubed.selection.selectAll
import kubed.shape.circle
import kubed.shape.text
import javafx.scene.paint.Color
import kubed.axis.axisBottom
import kubed.axis.axisLeft
import kubed.interpolate.interpolateNumber
import kubed.scale.LinearScale

class AxisDemo: Application() {
    override fun start(primaryStage: Stage?) {
        val padding = 20.0

        val root = Group()
        root.translateX = padding
        root.translateY = padding

        val width = 500.0
        val height = 300.0

        val data = listOf(listOf(5, 20),
                           listOf(480, 90),
                           listOf(250, 50),
                           listOf(100, 33),
                           listOf(330, 95),
                           listOf(410, 12),
                           listOf(475, 44),
                           listOf(25, 67),
                           listOf(85, 21),
                           listOf(220, 88),
                           listOf(600, 150))

        val xScale = LinearScale(::interpolateNumber).domain(listOf(0.0, data.map { it[0].toDouble() }.max() as Double))
                                                     .range(listOf(padding, width - padding * 2))

        val yScale = LinearScale(::interpolateNumber).domain(listOf(0.0, data.map { it[1].toDouble() }.max() as Double))
                                                     .range(listOf(height - padding, padding))

        val rScale = LinearScale(::interpolateNumber).domain(listOf(0.0, data.map { it[1].toDouble() }.max() as Double))
                                                     .range(listOf(2.0, 5.0))

        val circle = circle<List<Int>>().radius { d -> rScale(d[1].toDouble())}
                                       .translateX { d -> xScale(d[0].toDouble()) }
                                       .translateY { d -> yScale(d[1].toDouble()) }

        root.selectAll<Node>()
            .data(listOf(data))
            .enter()
            .append { d, _, _ -> circle(d as List<Int>) }

        val xAxis = axisBottom(xScale) {
            tickCount = 5
            formatter = { d -> d.toInt().toString() }
        }

        xAxis(root.emptySelection().append(fun(): Node { return Group() })
                                   .classed("axis")
                                   .translateY(height - padding))

        val yAxis = axisLeft(yScale) {
            tickCount = 4
            formatter = { d -> d.toInt().toString() }
        }
        yAxis(root.emptySelection().append(fun(): Node { return Group() })
                                   .classed("axis")
                                   .translateX(padding))

        val scene = Scene(root)
        primaryStage?.width = width + padding * 2
        primaryStage?.height = height + padding * 2

        primaryStage?.scene = scene
        primaryStage?.show()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(AxisDemo::class.java, *args)
        }
    }
}

