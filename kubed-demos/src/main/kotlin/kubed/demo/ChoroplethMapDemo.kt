import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.Tooltip
import javafx.scene.paint.Color
import javafx.stage.Stage
import kubed.color.scheme.schemeBlues
import kubed.geo.Feature
import kubed.geo.FeatureCollection
import kubed.geo.geoJson
import kubed.geo.path.geoPath
import kubed.geo.projection.albersUsa
import kubed.path.PathContext
import kubed.scale.scaleThreshold
import kubed.selection.selectAll
import java.io.File

class ChoroplethMapDemo : Application() {
    override fun start(stage: Stage?) {
        val width = 960.0
        val height = 600.0

        val scene = Scene(createMap())

        stage?.width = width
        stage?.height = height

        stage?.scene = scene
        stage?.show()
    }

    private fun createMap(): Group {
        val map = Group()

        val color = scaleThreshold<Color> {
            domain(kubed.array.range(2.0, 10.0).toList())
            range(schemeBlues(9))
        }

        val data = parse()
        val path = geoPath(albersUsa(), PathContext())
        geoJson(javaClass.getResource("data/us_counties_5m.json")) { geo ->
            val featureCollection = geo as FeatureCollection

            map.selectAll<Feature>()
               .data(featureCollection.features)
               .enter()
               .append { feature, _, _ -> path(feature) }
               .stroke(Color.WHITE)
               .strokeWidth(0.25)
               .fill { feature, _, _ -> color(data[feature.properties["GEO_ID"] as String] ?: 0.0) }
               .tooltip { feature, _, _ -> Tooltip("" + data[feature.properties["GEO_ID"] as String] + "%") }

            geoJson(javaClass.getResource("data/us_states_5m.json")) {
                val states = it as FeatureCollection

                map.selectAll<Feature>()
                        .data(states.features)
                        .enter()
                        .append { feature, _, _ -> path(feature) }
                        .stroke(Color.WHITE)
                        .fill(null)
            }
        }

        return map
    }

    private fun parse() = File(javaClass.getResource("data/unemployment.tsv").toURI())
            .readLines()
            .map {
                val s = it.split("\t")
                Pair("0500000US" + s[0], s[1].toDouble())
            }
            .toMap()
}

fun main(args: Array<String>) {
    Application.launch(ChoroplethMapDemo::class.java, *args)
}