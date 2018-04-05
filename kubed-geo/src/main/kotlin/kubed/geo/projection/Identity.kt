package kubed.geo.projection

import javafx.beans.property.SimpleDoubleProperty
import kubed.geo.FilterGeometryStream
import kubed.geo.GeometryStream

fun identity() = identity {}
fun identity(init: IdentityProjection.() -> Unit) = IdentityProjection().apply(init)

class IdentityProjection : ClippedProjection() {
    private val identity = { stream: GeometryStream -> stream }

    private var transform = identity

    override val precisionProperty = SimpleDoubleProperty(1.0)
    override var precision: Double
        get() = precisionProperty.get()
        set(value) = precisionProperty.set(value)

    override val scaleProperty = SimpleDoubleProperty(1.0)
    override var scale: Double
        get() = scaleProperty.get()
        set(k) = scaleProperty.set(k)

    override val translateXProperty = SimpleDoubleProperty(0.0)
    override var translateX: Double
        get() = translateXProperty.get()
        set(value) = translateXProperty.set(value)

    override val translateYProperty = SimpleDoubleProperty(0.0)
    override var translateY: Double
        get() = translateYProperty.get()
        set(value) = translateYProperty.set(value)

    init {
        scaleProperty.addListener { _ -> transform = scaleTranslate(scale, scale, translateX, translateY); reset() }
        translateXProperty.addListener { _ -> transform = scaleTranslate(scale, scale, translateX, translateY); reset() }
        scaleProperty.addListener { _ -> transform = scaleTranslate(scale, scale, translateX, translateY); reset() }
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