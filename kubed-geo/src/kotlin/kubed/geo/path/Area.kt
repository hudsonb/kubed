package kubed.geo.path

import kubed.geo.GeometryStream
import java.math.BigDecimal

class Area : GeometryStream {
    private var areaSum = BigDecimal.ZERO
    private var areaRingSum = BigDecimal.ZERO
    private var x00 = Double.NaN
    private var y00 = Double.NaN
    private var x0 = Double.NaN
    private var y0 = Double.NaN

    private var streamingPolygon = false
    private var firstPoint = false

    fun result(): Double {
        val a = areaSum.divide(2.toBigDecimal()).toDouble()
        areaSum = BigDecimal.ZERO
        return a
    }

    override fun polygonStart() {
        streamingPolygon = true
    }

    override fun polygonEnd() {
        streamingPolygon = false
        areaSum.add(areaRingSum.abs())
        areaRingSum = BigDecimal.ZERO
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
            areaRingSum = areaRingSum.add((y0 * x - x0 * y).toBigDecimal())
            x0 = x
            y0 = y
        }
    }
}