package kubed.geo.path

import javafx.scene.Node
import kubed.geo.GeoJSON
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

    operator fun invoke(geo: GeoJSON): Node {
        stream(geo, projection.stream(pathStream))
        return context()
    }

    fun area(geo: GeoJSON): Double {
        stream(geo, projection.stream(pathArea))
        return pathArea.result()
    }

    fun bounds(geo: GeoJSON): Array<DoubleArray> {
        stream(geo, projection.stream(pathBounds))
        return pathBounds.result()
    }

    fun centroid(geo: GeoJSON): DoubleArray {
        stream(geo, projection.stream(pathCentroid))
        return pathCentroid.result()
    }

    fun measure(geo: GeoJSON): Double {
        stream(geo, projection.stream(pathMeasure))
        return pathMeasure.result()
    }
}