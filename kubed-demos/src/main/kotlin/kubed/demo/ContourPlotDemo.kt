package kubed.demo

import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import kubed.contour.Contour
import kubed.contour.ContourGenerator
import kubed.contour.contours
import kubed.geo.path.geoPath
import kubed.geo.projection.identity
import kubed.interpolate.color.interpolateYlGnBu
import kubed.path.PathContext
import kubed.scale.scaleLinear
import kubed.scale.scaleLog
import kubed.selection.selectAll
import java.awt.Color.white
import java.lang.Math.pow

class ContourPlotDemo : Application() {
    override fun start(primaryStage: Stage?) {
        val margin = Insets(20.0, 10.0, 20.0, 10.0)

        val outerWidth = 960.0
        val outerHeight = 500.0
        val innerWidth = outerWidth - margin.left - margin.right
        val innerHeight = outerHeight - margin.top - margin.bottom

        val root = Group()
        root.prefWidth(innerWidth)
        root.prefHeight(innerHeight)
        root.translateX = margin.left + 30.0
        root.translateY = margin.top

        // Populate a grid of n×m values where -2 ≤ x ≤ 2 and -2 ≤ y ≤ 1.
        val n = 240
        val m = 125
        val values = DoubleArray(n * m)
        var j = 0.5
        var k = 0
        while(j < m) {
            var i = 0.5
            while(i < n) {
                values[k] = goldsteinPrice(i / n * 4 - 2, 1 - j / m * 3)
                ++i
                ++k
            }
            ++j
        }

       val t = (1..21).map { pow(2.0, it.toDouble()) }.toDoubleArray()

        val contours = contours(values) {
            columns = n
            rows = m
            thresholds = t
        }

        val color = scaleLog<Color>({ _, _ -> interpolateYlGnBu() }) {
            domain(listOf(t.min()!!, t.max()!!))
            range(listOf(Color.YELLOW, Color.GREEN, Color.BLUE))
        }

        val projection = identity {
            scale = innerWidth / n
        }

        val path = geoPath(projection, PathContext())
        root.selectAll<Contour>("*")
            .data(contours)
            .enter()
            .append { d, _, _ -> path(d.geometry) }
            .fill { d, _, _ -> color(d.value) }
            .stroke(Color.WHITE)
            .strokeWidth(0.5)

        val scene = Scene(root)
        primaryStage?.scene = scene
        primaryStage?.show()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(ContourPlotDemo::class.java, *args)
        }
    }

    private fun goldsteinPrice(x: Double, y: Double) =
            (1 + Math.pow(x + y + 1, 2.0) * (19 - 14 * x + 3 * x * x - 14 * y + 6 * x * x + 3 * y * y)) *
                    (30 + Math.pow(2 * x - 3 * y, 2.0) * (18 - 32 * x + 12 * x * x + 48 * y - 36 * x * y + 27 * y * y))
}