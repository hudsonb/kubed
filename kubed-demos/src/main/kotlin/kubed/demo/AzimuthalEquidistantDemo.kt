package kubed.demo

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.shape.Path
import javafx.stage.Stage
import kubed.geo.*
import kubed.geo.path.geoPath
import kubed.geo.projection.azimuthalEquidistant
import kubed.path.PathContext

/**
 * Demonstrates the [Azimuthal Equidistant projection](https://en.wikipedia.org/wiki/Azimuthal_equidistant_projection),
 * which is accessible via [azimuthalEquidistant].
 *
 * Based on https://bl.ocks.org/mbostock/3757110.
 */
class AzimuthalEquidistantDemo : Application() {
    override fun start(primaryStage: Stage?) {
        val root = Group()

        val width = 960.0
        val height = 960.0

        val projection = azimuthalEquidistant {
            scale = 150.0
            translateX =width / 2
            translateY = height / 2
            rotateX = 122.4194
            rotateY = -37.7749
            clipAngle = 180 - 1e-3
            precision = 0.1
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
    Application.launch(AzimuthalEquidistantDemo::class.java, *args)
}

