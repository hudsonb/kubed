package kubed.geo

open class FilterGeometryStream(val stream: GeometryStream) : GeometryStream {
    override fun point(x: Double, y: Double, z: Double) = stream.point(x, y, z)
    override fun lineStart() = stream.lineStart()
    override fun lineEnd() = stream.lineEnd()
    override fun polygonStart() = stream.polygonStart()
    override fun polygonEnd() = stream.polygonEnd()
    override fun sphere() = stream.sphere()
}