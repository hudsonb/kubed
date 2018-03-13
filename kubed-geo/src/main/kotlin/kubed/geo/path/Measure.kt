package kubed.geo.path

import kubed.geo.GeometryStream
import kubed.geo.math.Accumulator
import kubed.geo.math.sqrt

class Measure : GeometryStream {
    private val lengthAccumulator = Accumulator()
    private var lengthRing = false
    private var x00 = Double.NaN
    private var y00 = Double.NaN
    private var x0 = Double.NaN
    private var y0 = Double.NaN

    private var streamingLine = false
    private var firstPoint = false

    fun result(): Double {
        val r = lengthAccumulator.sum
        lengthAccumulator.set(0.0)
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
            lengthAccumulator += sqrt(x0 * x0 + y0 * y0)
        }

        x0 = x
        y0 = y
    }
}