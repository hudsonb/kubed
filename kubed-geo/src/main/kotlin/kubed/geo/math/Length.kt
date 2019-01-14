package kubed.geo.math

import kubed.geo.GeometryStream
import kubed.math.toRadians
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class Length : GeometryStream {
    private val accumulator = Accumulator()
    private var lambda0 = 0.0
    private var sinPhi0 = 0.0
    private var cosPhi0 = 0.0

    private var lineEnded = false
    private var firstPoint = false

    val length: Double
        get() = accumulator.sum

    override fun lineEnd() {
        lineEnded = true
    }

    override fun point(x: Double, y: Double, z: Double) {
        if(lineEnded) return

        val lambda = x.toRadians()
        val phi = y.toRadians()

        if(firstPoint) {
            firstPoint = false

            lambda0 = lambda

            sinPhi0 = sin(phi)
            cosPhi0 = cos(phi)
        }
        else {
            val sinPhi = sin(phi)
            val cosPhi = cos(phi)
            val delta = abs(lambda - lambda0)
            val cosDelta = cos(delta)
            val sinDelta = sin(delta)
            val x = cosPhi * sinDelta
            val y = cosPhi0 * sinPhi - sinPhi0 * cosPhi * cosDelta
            val z = sinPhi0 * sinPhi + cosPhi0 * cosPhi * cosDelta
            accumulator.add(atan2(sqrt(x * x + y * y), z))
            lambda0 = lambda
            sinPhi0 = sinPhi
            cosPhi0 = cosPhi
        }
    }
}