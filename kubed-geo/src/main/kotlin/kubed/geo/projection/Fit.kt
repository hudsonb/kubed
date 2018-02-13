package kubed.geo.projection

import javafx.geometry.Rectangle2D
import kubed.geo.GeoJson
import kubed.geo.path.Bounds
import kubed.geo.stream

fun fitExtent(projection: Projection, extent: Array<DoubleArray>, geo: GeoJson): Projection {
    val fitBounds = { b: Array<DoubleArray> ->
        val w = extent[1][0] - extent[0][0]
        val h = extent[1][1] - extent[0][1]
        val k = Math.min(w / (b[1][0] - b[0][0]), h / (b[1][1] - b[0][1]))
        val x = +extent[0][0] + (w - k * (b[1][0] + b[0][0])) / 2
        val y = +extent[0][1] + (h - k * (b[1][1] + b[0][1])) / 2
        with(projection) {
            scale = 150 * k
            translateX = x
            translateY = y
        }
    }

    return fit(projection, fitBounds, geo)
}

fun fitSize(projection: Projection, size: DoubleArray, geo: GeoJson) = fitExtent(projection, arrayOf(doubleArrayOf(0.0, 0.0), size), geo)

fun fitWidth(projection: Projection, width: Double, geo: GeoJson): Projection {
    val fitBounds = { b: Array<DoubleArray> ->
        val k = width / (b[1][0] - b[0][0])
        val x = (width - k * (b[1][0] + b[0][0])) / 2
        val y = -k * b[0][1]
        with(projection) {
            scale = 150 * k
            translateX = x
            translateY = y
        }
    }

    return fit(projection, fitBounds, geo)
}

fun fitHeight(projection: Projection, height: Double, geo: GeoJson): Projection {
    val fitBounds = { b: Array<DoubleArray> ->
        val k = height / (b[1][1] - b[1][0])
        val x = -k * b[0][0]
        val y = (height - k * (b[1][1] + b[0][1])) / 2
        with(projection) {
            scale = 150 * k
            translateX = x
            translateY = y
        }
    }

    return fit(projection, fitBounds, geo)
}

private fun fit(projection: Projection, fitBounds: (Array<DoubleArray>) -> Unit, geo: GeoJson): Projection {
    var clip: Rectangle2D? = null
    if(projection is ClippedProjection) clip = projection.clipExtent
    with(projection) {
        scale = 150.0
        translateX = 0.0
        translateY = 0.0
        if(clip != null) (this as ClippedProjection).clipExtent = null
    }

    val boundsStream = Bounds()
    stream(geo, projection.stream(boundsStream))
    if(clip != null) (projection as ClippedProjection).clipExtent = clip

    return projection
}

