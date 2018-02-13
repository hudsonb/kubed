package kubed.geo.path

import kubed.geo.GeometryStream
import kubed.geo.math.Accumulator
import kotlin.math.abs

class Area : GeometryStream {
    private var areaAccumulator = Accumulator()
    private var areaRingAccumulator = Accumulator()
    private var x00 = Double.NaN
    private var y00 = Double.NaN
    private var x0 = Double.NaN
    private var y0 = Double.NaN

    private var streamingPolygon = false
    private var firstPoint = false

    fun result(): Double {
        val a = areaAccumulator.sum / 2
        areaAccumulator.set(0.0)
        return a
    }

    override fun polygonStart() {
        streamingPolygon = true
    }

    override fun polygonEnd() {
        streamingPolygon = false
        areaAccumulator += abs(areaRingAccumulator.sum)
        areaRingAccumulator.set(0.0)
    }

    override fun lineStart() {
        if(streamingPolygon) firstPoint = true
    }

    override fun lineEnd() = point(x00, y00, 0.0)

    override fun point(x: Double, y: Double, z: Double) {
        if(firstPoint) {
            x00 = x
            x0 = x
            y00 = y
            y0 = y
            firstPoint = false
        }
        else {
            areaRingAccumulator += y0 * x - x0 * y
            x0 = x
            y0 = y
        }
    }
}