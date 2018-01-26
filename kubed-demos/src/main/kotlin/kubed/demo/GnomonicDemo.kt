package kubed.demo

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.shape.Path
import javafx.stage.Stage
import kubed.geo.*
import kubed.geo.path.geoPath
import kubed.geo.projection.gnomonic
import kubed.path.PathContext

/**
 * Demonstrates the [Gnomonic](http://en.wikipedia.org/wiki/Gnomonic_projection),
 * which is accessible via [gnomonic].
 *
 * Based on https://bl.ocks.org/mbostock/3757349.
 */
class GnomonicDemo : Application() {
    override fun start(primaryStage: Stage?) {
        val root = Group()

        val width = 960.0
        val height = 960.0

        val projection = gnomonic {
            clipAngle = 90 - 1e-3
            scale = 150.0
            translate = doubleArrayOf(width / 2, height / 2)
            precision = .1
        }

        val path = geoPath(projection, PathContext())
        val url = javaClass.getResource("/world.json")
        geoJson(url) { geo: GeoJson ->
            root.children += path(geo).apply {
                this as Path
                fill = Color.web("#222")
            }
        }

        val graticule = graticule()
        root.children += path(graticule.graticule()).apply {
            this as Path
            stroke = Color.web("#777", 0.5)
            strokeWidth = 0.5
        }

        root.children += path(Sphere()).apply {
            this as Path
            strokeWidth = 0.5
            stroke = Color.BLACK
        }

        val scene = Scene(root)
        primaryStage?.width = width
        primaryStage?.height = height

        primaryStage?.scene = scene
        primaryStage?.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(GnomonicDemo::class.java, *args)
}

