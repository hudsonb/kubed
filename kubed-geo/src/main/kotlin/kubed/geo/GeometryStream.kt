package kubed.geo

interface GeometryStream {
    fun point(x: Double, y: Double, z: Double) {}
    fun lineStart() {}
    fun lineEnd() {}
    fun polygonStart() {}
    fun polygonEnd() {}
    fun sphere() {}
}

fun stream(geo: GeoJson, stream: GeometryStream) {
    when(geo) {
        is Geometry<*> -> streamGeometry(geo, stream)
        is GeometryCollection -> streamGeometryCollection(geo, stream)
        is Feature -> streamGeometry(geo.geometry, stream)
        is FeatureCollection -> geo.features.forEach { streamGeometry(it.geometry, stream) }
        is Sphere -> streamSphere(stream)
    }
}

fun streamGeometry(g: Geometry<*>, stream: GeometryStream) {
    when(g) {
        is Point -> streamPoint(g, stream)
        is MultiPoint -> streamMultiPoint(g, stream)
        is LineString -> streamLineString(g, stream)
        is MultiLineString -> streamMultiLineString(g, stream)
        is Polygon -> streamPolygon(g, stream)
        is MultiPolygon -> streamMultiPolygon(g, stream)
    }
}

fun streamSphere(stream: GeometryStream) {
    stream.sphere()
}

fun streamPoint(point: Point, stream: GeometryStream) {
    val p = point.coordinates
    stream.point(p[0], p[1], p[2])
}

fun streamMultiPoint(mp: MultiPoint, stream: GeometryStream) {
    mp.coordinates.forEach { stream.point(it[0], it[1], it[2]) }
}

fun streamLineString(ls: LineString, stream: GeometryStream) {
    streamLine(ls.coordinates, stream, false)
}

fun streamMultiLineString(mls: MultiLineString, stream: GeometryStream) {
    mls.coordinates.forEach { streamLine(it, stream, false) }
}

fun streamPolygon(p: Polygon, stream: GeometryStream) {
    streamPoly(p.coordinates, stream)
}

fun streamMultiPolygon(mp: MultiPolygon, stream: GeometryStream) {
    mp.coordinates.forEach { streamPoly(it, stream) }
}

fun streamGeometryCollection(gc: GeometryCollection, stream: GeometryStream) {
    gc.geometries.forEach { streamGeometry(it, stream) }
}

private fun streamLine(coords: List<Position>, stream: GeometryStream, closed: Boolean) {
    val n = if(closed) coords.size - 1 else coords.size

    with(stream) {
        lineStart()
        for(i in 0 until n) {
            val p = coords[i]
            point(p.longitude, p.latitude, p.elevation)
        }
        lineEnd()
    }
}

private fun streamPoly(coords: List<List<Position>>, stream: GeometryStream) {
    with(stream) {
        polygonStart()
        coords.forEach { streamLine(it, stream, true) }
        polygonEnd()
    }
}