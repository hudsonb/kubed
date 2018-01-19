package kubed.geo

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.net.URL

interface GeoJSON {
    val type: String
}

interface Geometry<out T> : GeoJSON {
    val coordinates: T
}

data class Position(val longitude: Double, val latitude: Double, val elevation: Double = 0.0) {
    operator fun get(i: Int) = when(i) {
        0 -> longitude
        1 -> latitude
        2 -> elevation
        else -> throw IllegalArgumentException()
    }
}

data class Point(override val coordinates: Position) : Geometry<Position> {
    constructor(x: Double, y: Double, z: Double = 0.0) : this(Position(x, y, z))

    override val type = "Point"
}

data class LineString(override val coordinates: List<Position>) : Geometry<List<Position>> {
    override val type = "LineString"
}

data class Polygon(override val coordinates: List<List<Position>>) : Geometry<List<List<Position>>> {
    override val type = "Polygon"
}

data class MultiPoint(override val coordinates: List<Position>) : Geometry<List<Position>> {
    override val type = "MultiPoint"
}

data class MultiLineString(override val coordinates: List<List<Position>>) : Geometry<List<List<Position>>> {
    override val type = "MultiLineString"
}

data class MultiPolygon(override val coordinates: List<List<List<Position>>>) : Geometry<List<List<List<Position>>>> {
    override val type = "MultiPolygon"
}

data class GeometryCollection(val geometries: List<Geometry<*>>) : GeoJSON {
    override val type = "GeometryCollection"
}

data class Feature(val geometry: Geometry<*>, val properties: Map<String, *>) : GeoJSON {
    override val type = "Feature"
}

data class FeatureCollection(val features: List<Feature>) : GeoJSON {
    override val type = "FeatureCollection"
}

class Sphere : GeoJSON {
    override val type = "Sphere"
}

fun geoJson(url: URL, callback: (GeoJSON) -> Unit) {
    val type = object : TypeToken<GeoJSON>() {}
    val gson = Gson()
    val geo = gson.fromJson(InputStreamReader(url.openStream()), FeatureCollection::class.java)
    println(geo.type)
}

fun main(args: Array<String>) {
    geoJson(URL("http://eric.clst.org/assets/wiki/uploads/Stuff/gz_2010_us_040_00_500k.json"), {_ ->})
}