package kubed.geo

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import javafx.application.Platform
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes(
        Type(value = Point::class, name = "Point"),
        Type(value = LineString::class, name = "LineString"),
        Type(value = Polygon::class, name="Polygon"),
        Type(value = MultiPoint::class, name="MultiPoint"),
        Type(value = MultiLineString::class, name="MultiLineString"),
        Type(value = MultiPolygon::class, name="MultiPolygon"),
        Type(value = GeometryCollection::class, name="GeometryCollection"),
        Type(value = Feature::class, name="Feature"),
        Type(value = FeatureCollection::class, name="FeatureCollection"))
interface GeoJSON {
    val type: String
}

interface Geometry<out T> : GeoJSON {
    val coordinates: T
}

@JsonSerialize(using = PositionSerializer::class)
@JsonDeserialize(using = PositionDeserializer::class)
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

    @JsonIgnore
    override val type = "Point"
}

data class LineString(override val coordinates: List<Position>) : Geometry<List<Position>> {
    @JsonIgnore
    override val type = "LineString"
}

data class Polygon(override val coordinates: List<List<Position>>) : Geometry<List<List<Position>>> {
    @JsonIgnore
    override val type = "Polygon"
}

data class MultiPoint(override val coordinates: List<Position>) : Geometry<List<Position>> {
    @JsonIgnore
    override val type = "MultiPoint"
}

data class MultiLineString(override val coordinates: List<List<Position>>) : Geometry<List<List<Position>>> {
    @JsonIgnore
    override val type = "MultiLineString"
}

data class MultiPolygon(override val coordinates: List<List<List<Position>>>) : Geometry<List<List<List<Position>>>> {
    @JsonIgnore
    override val type = "MultiPolygon"
}

data class GeometryCollection(val geometries: List<Geometry<*>>) : GeoJSON {
    @JsonIgnore
    override val type = "GeometryCollection"
}

data class Feature(val geometry: Geometry<*>, val properties: Map<String, *>) : GeoJSON {
    @JsonIgnore
    override val type = "Feature"
}

data class FeatureCollection(val features: List<Feature>) : GeoJSON {
    @JsonIgnore
    override val type = "FeatureCollection"
}

class Sphere : GeoJSON {
    @JsonIgnore
    override val type = "Sphere"
}

private class PositionSerializer(t: Class<Position>?) : StdSerializer<Position>(t) {
    constructor() : this(null)

    override fun serialize(value: Position, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartArray()
        gen.writeNumber(value.longitude)
        gen.writeNumber(value.latitude)
        if(value.elevation != 0.0) gen.writeNumber(value.elevation)
        gen.writeEndArray()
    }
}

private class PositionDeserializer(t: Class<Position>?) : StdDeserializer<Position>(t) {
    constructor() : this(null)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Position {

        var lon: Double? = null
        var lat: Double? = null
        var elevation: Double? = null

        var t = p.nextToken()
        while(t != JsonToken.END_ARRAY) {
            when {
                lon == null -> lon = p.valueAsDouble
                lat == null -> lat = p.valueAsDouble
                elevation == null -> elevation = p.valueAsDouble
            }

            t = p.nextToken()
        }

        if(lon == null || lat == null) throw JsonParseException(p, "Lon/Lat not provided for Position")

        return Position(lon, lat, if(elevation != null) elevation else 0.0)
    }
}

fun geoJson(url: URL, service: ExecutorService = ForkJoinPool.commonPool(), callback: (GeoJSON) -> Unit) {
    service.submit {
        val mapper = jacksonObjectMapper()
        val geo = mapper.readValue<GeoJSON>(url, GeoJSON::class.java)
        Platform.runLater { callback(geo) }
    }
}

fun main(args: Array<String>) {
    //geoJson(URL("http://eric.clst.org/assets/wiki/uploads/Stuff/gz_2010_us_040_00_500k.json"), {_ ->})
    val geojson = """{ "type": "LineString", "coordinates": [[0, 0], [10, 10]] }"""

    val mapper = jacksonObjectMapper()

    val ls = LineString(arrayListOf(Position(0.0, 0.0), Position(10.0, 10.0)))
    println(mapper.writeValueAsString(ls))

    val g = mapper.readValue<GeoJSON>(geojson, GeoJSON::class.java)
    println(mapper.writeValueAsString(g))
}