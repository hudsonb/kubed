package kubed.geo.path

import kubed.geo.GeometryStream
import java.math.BigDecimal
import kotlin.math.sqrt

class Measure : GeometryStream {
    private var lengthSum = BigDecimal.ZERO
    private var lengthRing = false
    private var x00 = Double.NaN
    private var y00 = Double.NaN
    private var x0 = Double.NaN
    private var y0 = Double.NaN

    private var streamingLine = false
    private var firstPoint = false

    fun result(): Double {
        val r = lengthSum.toDouble()
        lengthSum = BigDecimal.ZERO
        return r
    }

    override fun lineStart() {
        streamingLine = true
        firstPoint = true
    }

    override fun lineEnd() {
        if(lengthRing) point(x00, y00, 0.0)
        streamingLine = false
    }

    override fun polygonStart() {
        lengthRing = true
    }

    override fun polygonEnd() {
        lengthRing = false
    }

    override fun point(x: Double, y: Double, z: Double) {
        if(!streamingLine) return

        if(firstPoint) {
            x00 = x
            y00 = y
        }
        else {
            x0 -= x
            y0 -= y
            lengthSum = lengthSum.add(sqrt(x0 * x0 + y0 * y0).toBigDecimal())
        }

        x0 = x
        y0 = y
    }
}