package kubed.demo

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.shape.Path
import javafx.stage.Stage
import kubed.geo.*
import kubed.geo.path.geoPath
import kubed.geo.projection.aitoff
import kubed.path.PathContext

class AitoffDemo : Application() {
    override fun start(primaryStage: Stage?) {
        val root = Group()

        val width = 960.0
        val height = 500.0

        val projection = aitoff {
            scale = 150.0
            translate = doubleArrayOf(width / 2, height / 2)
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

fun main(vararg args: String) {
    Application.launch(AitoffDemo::class.java, *args)
}

