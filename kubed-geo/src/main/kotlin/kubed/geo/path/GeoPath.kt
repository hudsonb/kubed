package kubed.geo.path

import javafx.scene.Node
import kubed.geo.GeoJson
import kubed.geo.projection.Projection
import kubed.geo.stream
import kubed.path.Context

fun geoPath(projection: Projection, context: Context) = GeoPath(projection, context)

class GeoPath(val projection: Projection, val context: Context) {
    private val pathArea = Area()
    private val pathBounds = Bounds()
    private val pathCentroid = Centroid()
    private val pathMeasure = Measure()
    private val pathStream: PathStream = PathStream(context)

    var pointRadius: Double
        get() = pathStream.pointRadius
        set(value) {
            pathStream.pointRadius = value
        }

    operator fun invoke(geo: GeoJson): Node {
        stream(geo, projection.stream(pathStream))
        return context()
    }

    fun area(geo: GeoJson): Double {
        stream(geo, projection.stream(pathArea))
        return pathArea.result()
    }

    fun bounds(geo: GeoJson): Array<DoubleArray> {
        stream(geo, projection.stream(pathBounds))
        return pathBounds.result()
    }

    fun centroid(geo: GeoJson): DoubleArray {
        stream(geo, projection.stream(pathCentroid))
        return pathCentroid.result()
    }

    fun measure(geo: GeoJson): Double {
        stream(geo, projection.stream(pathMeasure))
        return pathMeasure.result()
    }
}