package kubed.demo

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.stage.Stage
import kubed.geo.Sphere
import kubed.geo.graticule
import kubed.geo.path.geoPath
import kubed.geo.projection.*
import kubed.math.TAU
import kubed.path.PathContext
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
//            precision = .1
//        }

//        val projection = equirectangular {
//            scale = height / PI
//            translate = doubleArrayOf(width / 2, height / 2)
//        }

//        val projection = mercator {
//            scale = (width - 3) / TAU
//            translate = doubleArrayOf(width / 2, height / 2)
//        }

//        val projection = transverseMercator {
//            scale = (width - 3) / TAU
//            translate = doubleArrayOf(width / 2, height / 2)
//        }

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

        val projection = stereographic {
            scale = 245.0
            translate = doubleArrayOf(width / 2, height / 2)
            rotate = doubleArrayOf(-20.0, 0.0)
            clipAngle = 180 - 1e-4
            clipExtent = arrayOf(doubleArrayOf(0.0, 0.0), doubleArrayOf(width, height))
            precision = .1
        }

        val path = geoPath(projection, PathContext())
        root.children += path(graticule().graticule())
        //graticule().lines().forEach { root.children += path(it) }
        //root.children += path(graticule().outline())
        root.children += path(Sphere())

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

