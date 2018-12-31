import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.Tooltip
import javafx.scene.paint.Color
import javafx.scene.shape.Shape
import javafx.stage.Stage
import kubed.geo.Feature
import kubed.geo.Sphere
import kubed.geo.graticule
import kubed.geo.path.geoPath
import kubed.geo.projection.equalEarth
import kubed.path.PathContext
import kubed.selection.selectAll
import kubed.shapefile.ShapefileReader

class ShapefileDemo : Application() {
    override fun start(stage: Stage?) {
        val width = 960.0
        val height = 600.0

        val map = createMap()
        map.translateX = width / 2
        map.translateY = height / 2
        val scene = Scene(map)

        stage?.width = width
        stage?.height = height

        stage?.scene = scene
        stage?.show()
    }

    private fun createMap(): Group {
        val map = Group()

        val features = ArrayList<Feature>()
        val reader = ShapefileReader(javaClass.getResourceAsStream("/data/TM_WORLD_BORDERS_SIMPL-0.3.shp"),
                                     javaClass.getResourceAsStream("/data/TM_WORLD_BORDERS_SIMPL-0.3.dbf"))
        var feature = reader.nextFeature()
        while(feature != null) {
            features += feature
            feature = reader.nextFeature()
        }

        val path = geoPath(equalEarth(), PathContext())
        map.children += path(Sphere())
        map.children += path(graticule().graticule()).apply { this as Shape; stroke = Color.LIGHTGREY; strokeWidth = 0.5 }
        map.selectAll<Feature>()
                .data(features)
                .enter()
                .append { d, _, _ -> path(d) }
                .stroke(Color.WHITESMOKE)
                .strokeWidth(0.25)
                .fill(Color.BLACK)
                .tooltip { d, _, _ -> Tooltip("" + d.properties["POP2005"]) }

        return map
    }
}

fun main(args: Array<String>) {
    Application.launch(ShapefileDemo::class.java, *args)
}