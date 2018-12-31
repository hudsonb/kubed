package kubed.demo

import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import kubed.axis.axisBottom
import kubed.axis.axisLeft
import kubed.interpolate.interpolateRound
import kubed.scale.scaleLinear
import kubed.selection.selectAll
import kubed.shape.area
import kubed.shape.curve.*

class AreaDemo: Application() {
    override fun start(primaryStage: Stage?) {
        val margin = Insets(20.0, 10.0, 20.0, 10.0)

        val outerWidth = 960.0
        val outerHeight = 500.0
        val innerWidth = outerWidth - margin.left - margin.right
        val innerHeight = outerHeight - margin.top - margin.bottom

        val root = Group()
        root.prefWidth(outerWidth)
        root.prefHeight(outerHeight)
        root.translateX = margin.left + 30.0
        root.translateY = margin.top

        val data = listOf(4, 2, 6, 3, 3, 7, 9, 2, 1, 6)

        val xScale = scaleLinear(::interpolateRound) {
            range(0.0, innerWidth)
            domain(0.0, data.size - 1.0)
        }

        val yScale = scaleLinear(::interpolateRound) {
            range(innerHeight, 0.0)
            domain(0.0, 10.0)
        }

        val xAxis = axisBottom(xScale)
        xAxis(root.selectAll<Unit>(".xAxis")
                  .append { _, _, _ -> Group() }
                  .classed("axis", "xAxis")
                  .translateY(innerHeight))

        val yAxis = axisLeft(yScale)
        yAxis(root.selectAll<Unit>(".yAxis")
                  .append { _, _, _ -> Group() }
                  .classed("axis", "yAxis"))

        val area = area<Int> {
            x { _, i, _ -> xScale(i.toDouble()) }
            y1 { d, _, _ -> yScale(d.toDouble()) }
            y0(yScale(0.0))
            curve(curveNatural())
            fill(Color.STEELBLUE)
        }

        root.children += area(data)

        val scene = Scene(root)
        primaryStage?.scene = scene
        primaryStage?.show()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(AreaDemo::class.java, *args)
        }
    }
}