package kubed.geo.path

import kubed.geo.GeometryStream
import kubed.math.TAU
import kubed.path.Context

class PathStream(val context: Context) : GeometryStream {
    var pointRadius = 4.5

    private var line = false
    private var point = -1

    override fun polygonStart() {
        line = true
    }

    override fun polygonEnd() {
        line = false
    }

    override fun lineStart() {
        point = 0
    }

    override fun lineEnd() {
        if(line) context.closePath()
        point = -1
    }

    override fun point(x: Double, y: Double, z: Double) {
        when (point) {
            0 -> {
                context.moveTo(x, y)
                point = 1
            }
            1 -> context.lineTo(x, y)
            else -> {
                context.moveTo(x + pointRadius, y)
                context.arc(x, y, pointRadius, 0.0, TAU, false)
            }
        }
    }
}