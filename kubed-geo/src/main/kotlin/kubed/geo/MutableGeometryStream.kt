package kubed.geo

open class MutableGeometryStream : GeometryStream {
    var point: (Double, Double, Double) -> Unit = ::noop
    var lineStart: () -> Unit = ::noop
    var lineEnd: () -> Unit = ::noop
    var polygonStart: () -> Unit = ::noop
    var polygonEnd: () -> Unit = ::noop
    var sphere: () -> Unit = ::noop

    final override fun point(x: Double, y: Double, z: Double) = point.invoke(x, y, z)
    final override fun lineStart() = lineStart.invoke()
    final override fun lineEnd() = lineEnd.invoke()
    final override fun polygonStart() = polygonStart.invoke()
    final override fun polygonEnd() = polygonEnd.invoke()
    final override fun sphere() = sphere.invoke()

    final fun noop() {}
    final fun noop(x: Double, y: Double, z: Double) {}
}