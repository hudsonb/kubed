package kubed.demo

import javafx.application.Application
import javafx.application.Application.launch
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.shape.Path
import javafx.stage.Stage
import kubed.geo.*
import kubed.geo.path.geoPath
import kubed.geo.projection.*
import kubed.math.TAU
import kubed.path.PathContext
import kubed.timer.timer
import java.io.File
import java.lang.Math.random
import kotlin.math.PI

class BasicMapDemo : Application() {
    override fun start(primaryStage: Stage?) {
        val root = Group()

        val width = 960.0
        val height = 960.0

//        val projection = orthographic {
//            scale = 475.0
//            translate = doubleArrayOf(width / 2, height / 2)
//            clipAngle = 90.0
//           // precision = .1
//            rotate = doubleArrayOf(90.0, -10.0)
//        }

//        val projection = equirectangular {
//            scale = height / PI
//            translate = doubleArrayOf(width / 2, height / 2)
//        }

//        val projection = mercator {
//            scale = (width - 3) / TAU
//            translate = doubleArrayOf(width / 2, height / 2)
//        }

        val projection = transverseMercator {
            scale = (width - 3) / TAU
            translate = doubleArrayOf(width / 2, height / 2)
        }

//        val projection = azimuthalEqualArea {
//            scale = 239.0
//            rotate = doubleArrayOf(0.0, 90.0)
//            clipAngle = 180 - 1e-3
//            translate = doubleArrayOf(width / 2, height / 2)
//            precision = 0.1
//        }

//        val projection = azimuthalEquidistant {
//            scale = 150.0
//            translate = doubleArrayOf(width / 2, height / 2)
//            rotate = doubleArrayOf(122.4194, -37.7749)
//            clipAngle = 180 - 1e-3
//            precision = 0.1
//        }

//        val projection = gnomonic {
//            clipAngle = 90 - 1e-3
//            scale = 150.0
//            translate = doubleArrayOf(width / 2, height / 2)
//            precision = .1
//        }

        // This has a clipping issue or something
     //   val projection = conicEqualArea()

//        val projection = conicEquidistant {
//            center = doubleArrayOf(0.0, 15.0)
//            scale = 128.0
//            translate = doubleArrayOf(width / 2, height / 2)
//            precision = .1
//        }

//        val projection = stereographic {
//            scale = 245.0
//            translate = doubleArrayOf(width / 2, height / 2)
//            rotate = doubleArrayOf(-20.0, 0.0)
//            clipAngle = 180 - 1e-4
//            clipExtent = arrayOf(doubleArrayOf(0.0, 0.0), doubleArrayOf(width, height))
//            precision = .1
//        }

       // val projection = albers()

//        val projection = naturalEarth {
//            scale = 167.0
//            translate = doubleArrayOf(width / 2, height / 2)
//            precision = .1
//        }

        val path = geoPath(projection, PathContext())
        //val url = URL("https://d2ad6b4ur7yvpq.cloudfront.net/naturalearth-3.3.0/ne_110m_land.geojson")

        val graticule = graticule().graticule()
        val url = File("/Users/hudsonb/Downloads/world.json").toURI().toURL()
        geoJson(url) { geo: GeoJSON ->
            geo as FeatureCollection

            root.children += path(geo).apply {
                this as Path
                fill = Color.web("#dadac4")
            }

            root.children += path(graticule).apply {
                this as Path
                stroke = Color.rgb(119, 119, 119, .5)
                strokeWidth = 0.5
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
            launch(BasicMapDemo::class.java, *args)
        }
    }
}

