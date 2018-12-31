package kubed.shapefile.shp

import kubed.collections.slice
import kubed.geo.*
import kubed.shapefile.io.MixedEndianInputStream
import kubed.util.isTruthy
import java.io.EOFException
import java.io.InputStream

class ShpReader(input: InputStream) {
    private val stream = MixedEndianInputStream(input.buffered())

    init {
        // Skip header
        stream.skipBytes(100)
    }

    fun nextGeometry(): Geometry<*>? {
        return try {
            val type = stream.readIntLe()
            when (type) {
                1 -> parsePoint(stream)
                3 -> parsePolyline(stream)
                5 -> parsePolygon(stream)
                8 -> parseMultiPoint(stream)
                else -> nextGeometry()
            }
        }
        catch (e: EOFException) {
            null
        }
    }

    private fun parsePosition(stream: MixedEndianInputStream): Position {
        val x = stream.readDoubleLe()
        val y = stream.readDoubleLe()
        return Position(x, y)
    }

    private fun parsePoint(stream: MixedEndianInputStream) = Point(parsePosition(stream))

    private fun parseMultiPoint(stream: MixedEndianInputStream): MultiPoint {
        parseBBox(stream)

        val numPoints = stream.readIntLe()
        val points = ArrayList<Position>(numPoints)
        repeat(numPoints) { points += parsePosition(stream) }
        return MultiPoint(points)
    }

    private fun parsePolyline(stream: MixedEndianInputStream): Geometry<*> {
        parseBBox(stream)

        val numParts = stream.readIntLe()
        val numPoints = stream.readIntLe()

        val parts = IntArray(numParts) { stream.readIntLe() }
        val points = ArrayList<Position>(numPoints)
        repeat(numPoints) { points += parsePosition(stream) }

        return when (numParts) {
            1 -> LineString(points.toList())
            else -> MultiLineString(parts.mapIndexed { j, i ->
                points.slice(i, parts.getOrElse(j + 1) { points.size })
            })
        }
    }

    private fun parsePolygon(stream: MixedEndianInputStream): Geometry<*> {
        parseBBox(stream)

        val numParts = stream.readIntLe()
        val numPoints = stream.readIntLe()

        val parts = IntArray(numParts) { stream.readIntLe() }
        val points = ArrayList<Position>(numPoints)
        repeat(numPoints) { points += parsePosition(stream) }

        val polygons = ArrayList<MutableList<List<Position>>>()
        val holes = ArrayList<List<Position>>()
        parts.forEachIndexed { j, i ->
            val ring = points.slice(i, parts.getOrElse(j + 1) { points.size - 1 })
            if(ringClockwise(ring)) polygons.add(mutableListOf(ring))
            else holes.add(ring)
        }

        holes.forEach { hole ->
            val p = polygons.find { ringContainsAny(it[0], hole) }
            if(p != null) p.add(hole)
            else polygons.add(mutableListOf(hole))
        }

        return when(polygons.size) {
            1 -> Polygon(polygons[0])
            else -> MultiPolygon(polygons)
        }
    }

    private fun ringClockwise(ring: List<Position>): Boolean {
        val n = ring.size
        if (n < 4) return false

        var area = ring[n - 1][1] * ring[0][0] - ring[n - 1][0] * ring[0][1]
        for (i in 1 until n) area += ring[i - 1][1] * ring[i][0] - ring[i - 1][0] * ring[i][1]
        return area >= 0
    }


    private fun ringContainsAny(ring: List<Position>, hole: List<Position>): Boolean {
        for (i in 0 until hole.size) {
            val c = ringContains(ring, hole[i])
            if (c.isTruthy()) return c > 0
        }

        return false
    }

    private fun ringContains(ring: List<Position>, point: Position): Int {
        val x = point.longitude
        val y = point.latitude
        var contains = -1

        var j = ring.size - 1
        for (i in ring.indices) {
            val pi = ring[i]
            val xi = pi[0]
            val yi = pi[1]
            val pj = ring[j]
            val xj = pj[0]
            val yj = pj[1]
            if (segmentContains(pi, pj, point)) return 0
            if (((yi > y) != (yj > y)) && ((x < (xj - xi) * (y - yi) / (yj - yi) + xi)))
                contains = -contains

            j = i
        }

        return contains
    }

    private fun segmentContains(p0: Position, p1: Position, p2: Position): Boolean {
        val x20 = p2[0] - p0[0]
        val y20 = p2[1] - p0[1]
        if (x20 == 0.0 && y20 == 0.0) return true

        val x10 = p1[0] - p0[0]
        val y10 = p1[1] - p0[1]
        if (x10 == 0.0 && y10 == 0.0) return false

        val t = (x20 * x10 + y20 * y10) / (x10 * x10 + y10 * y10)
        return when {
            t == 0.0 || t == 1.0 -> true
            t < 0 || t > 1 -> false
            else -> t * x10 == x20 && t * y10 == y20
        }
    }

    private fun parseBBox(stream: MixedEndianInputStream) = DoubleArray(4) { stream.readDoubleLe() }
}
