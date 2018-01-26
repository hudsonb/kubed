package kubed.demo

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.shape.Path
import javafx.stage.Stage
import kubed.geo.*
import kubed.geo.path.geoPath
import kubed.geo.projection.*
import kubed.path.PathContext
import kubed.timer.timer
import java.io.File

class SeeThroughGlobeDemo : Application() {
    override fun start(primaryStage: Stage?) {
        val root = Group()

        val width = 960.0
        val height = 960.0

        val projection = orthographic {
            scale = 475.0
            translate = doubleArrayOf(width / 2, height / 2)
            clipAngle = 90.0
            // precision = .1
            rotate = doubleArrayOf(90.0, -10.0)
        }

        val path = geoPath(projection, PathContext())

        val graticule = graticule().graticule()
        val url = File("/Users/hudsonb/Downloads/world.json").toURI().toURL()
        geoJson(url) { geo: GeoJson ->
            geo as FeatureCollection

            val speed = -1e-2
            timer { elapsed ->
                root.children.clear()

                projection.rotate = doubleArrayOf(speed * elapsed, -15.0)
                projection.clipAngle = 180.0

                root.children += path(geo).apply {
                    this as Path
                    fill = Color.web("#dadac4")
                }

                root.children += path(graticule).apply {
                    this as Path
                    stroke = Color.rgb(119, 119, 119, .5)
                    strokeWidth = 0.5
                }

                projection.clipAngle = 90.0

                root.children += path(geo).apply {
                    this as Path
                    stroke = Color.web("#000")
                    strokeWidth = 0.5
                    fill = Color.web("#737368")
                }
            }
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

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(SeeThroughGlobeDemo::class.java, *args)
        }
    }
}

