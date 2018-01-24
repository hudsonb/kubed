package kubed.geo.clip

import kubed.geo.GeometryStream

interface Clip {
    val start: DoubleArray

    fun isVisible(x: Double, y: Double): Boolean

    fun clipLine(stream: GeometryStream): IntersectStream
    fun interpolate(from: DoubleArray?, to: DoubleArray?, direction: Int, stream: GeometryStream)
}