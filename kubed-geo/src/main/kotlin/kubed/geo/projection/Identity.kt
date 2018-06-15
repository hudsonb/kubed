package kubed.geo.projection

import javafx.beans.property.SimpleDoubleProperty
import kubed.geo.FilterGeometryStream
import kubed.geo.GeometryStream

fun identity() = identity {}
fun identity(init: IdentityProjection.() -> Unit) = IdentityProjection().apply(init)

class IdentityProjection : ClippedProjection() {
    private val identity = { stream: GeometryStream -> stream }

    private var transform = identity

    override var precision = 1.0

    override var scale = 1.0
        set(value) {
            field = value
            transform = scaleTranslate(scale, scale, translateX, translateY)
            reset()
        }

    override var translateX = 0.0
        set(value) {
            field = value
            transform = scaleTranslate(scale, scale, translateX, translateY)
            reset()
        }

    override var translateY = 0.0
        set(value) {
            field = value
            transform = scaleTranslate(scale, scale, translateX, translateY)
            reset()
        }

    override fun invoke(point: DoubleArray) = point

    override fun invert(coordinates: DoubleArray) = coordinates

    override fun stream(forStream: GeometryStream): GeometryStream {
        var stream = getCachedStream(forStream)
        if(stream == null) {
            stream = transform(postclip(forStream))
            cache(forStream, stream)
        }

        return stream
    }

    private fun scaleTranslate(kx: Double, ky: Double, tx: Double, ty: Double) = when {
        (kx == 1.0 && ky == 1.0 && tx == 0.0 && ty == 0.0) -> identity
        else -> { stream: GeometryStream ->
                    object : FilterGeometryStream(stream) {
                        override fun point(x: Double, y: Double, z: Double) = stream.point(x * kx + tx, y * ky + ty, z)
                    }
                }
    }
}