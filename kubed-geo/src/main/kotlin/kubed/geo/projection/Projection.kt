package kubed.geo.projection

import javafx.geometry.Rectangle2D
import kubed.geo.FilterGeometryStream
import kubed.geo.GeoJson
import kubed.geo.GeometryStream
import kubed.geo.Position
import kubed.geo.clip.clipAntimeridian
import kubed.geo.clip.clipCircle
import kubed.geo.clip.clipRectangle
import kubed.math.toDegrees
import kubed.math.toRadians
import kubed.timer.timer
import java.util.concurrent.CopyOnWriteArrayList

fun projection(projector: Projector) = projection(projector) {}
fun projection(projector: Projector, init: MutableProjection.() -> Unit) = MutableProjection(projector).apply(init)

interface Projection {
    var precision: Double

    var scale: Double

    var translateX: Double

    var translateY: Double

    operator fun invoke(point: DoubleArray): DoubleArray
    fun invert(coordinates: DoubleArray): DoubleArray
    fun stream(forStream: GeometryStream): GeometryStream

    fun fitExtent(extent: Array<DoubleArray>, geo: GeoJson) = kubed.geo.projection.fitExtent(this, extent, geo)
    fun fitSize(size: DoubleArray, geo: GeoJson) = kubed.geo.projection.fitSize(this, size, geo)
    fun fitWidth(width: Double, geo: GeoJson) = kubed.geo.projection.fitWidth(this, width, geo)
    fun fitHeight(height: Double, geo: GeoJson) = kubed.geo.projection.fitHeight(this, height, geo)

    fun invalidate() {}

    fun addListener(listener: (Projection) -> Unit)
    fun removeListener(listener: (Projection) -> Unit)
}

abstract class AbstractProjection : Projection {
    private var invalidated = false

    private val listeners = CopyOnWriteArrayList<(Projection) -> Unit>()

    init {
        timer {
            if(invalidated) {
                invalidated = false
                fireProjectionChanged()
            }
        }
    }

    /**
     * Invalidates the projection, causing a projection change event to be sent to each listener on the next frame.
     */
    override fun invalidate() {
        invalidated = true
    }

    /**
     * Adds the given listener to this projection. The listener is called only once
     * per frame, and is guaranteed to be called from the JavaFX thread.
     */
    override fun addListener(listener: (Projection) -> Unit) {
        if(!listeners.contains(listener)) listeners.add(listener)
    }

    /**
     * Removes the given listener from this projection.
     */
    override fun removeListener(listener: (Projection) -> Unit) {
        listeners.remove(listener)
    }

    private fun fireProjectionChanged() = ArrayList(listeners).forEach { it(this) }
}

abstract class StreamCacheProjection : AbstractProjection() {
    private var cache: GeometryStream? = null
    private var cacheStream: GeometryStream? = null

    protected fun getCachedStream(forStream: GeometryStream): GeometryStream? = when {
        cache != null && cacheStream == forStream -> cache
        else -> null
    }

    protected fun cache(forStream: GeometryStream, stream: GeometryStream) {
        cache = stream
        cacheStream = forStream
    }

    /**
     * Invalidates the cached streams.
     *
     * This should be invoked by subclasses whenever a change has been made which requires new streams be created.
     */
    protected fun reset() {
        cache = null
        cacheStream = null
    }
}

abstract class ClippedProjection : StreamCacheProjection() {
    // Clip Angle
    var clipAngle = Double.NaN
        set(value) {
            field = value

            preclip = if(clipAngle.isNaN()) clipAntimeridian()
            else clipCircle(clipAngle.toRadians())

            invalidate()
        }

    protected var preclip: (GeometryStream) -> GeometryStream = clipAntimeridian()
        set(value) {
            field = value
            reset()
        }

    // Clip extent
    private val identity = { stream: GeometryStream -> stream }
    protected var postclip = identity
    open var clipExtent: Rectangle2D? = null
        set(extent) {
            field = extent
            postclip = if(extent == null) identity
            else clipRectangle(extent.minX, extent.minY, extent.maxX, extent.maxY)

            reset()
            invalidate()
        }
}

open class MutableProjection(protected var projector: Projector): ClippedProjection() {
    // Scale
    override var scale = 150.0
        set(value) {
            field = value
            recenter()
        }

    // Translate
    override var translateX = 0.0
        set(value) {
            field = value
            recenter()
        }

    override var translateY = 0.0
        set(value) {
            field = value
            recenter()
        }

    // Center
    private var dx = 0.0
    private var dy = 0.0

    open var center = Position(0.0, 0.0)
        set(value) {
            field = value
            recenter()
        }

    // Rotate
    private lateinit var rotator: Transform

    var rotateX = 0.0
        set(value) {
            field = value
            recenter()
        }

    var rotateY = 0.0
        set(value) {
            field = value
            recenter()
        }

    var rotateZ = 0.0
        set(value) {
            field = value
            recenter()
        }

    private lateinit var projectRotate: Transform

    private val projectTransform = object : Transform {
        override fun invoke(lambda: Double, phi: Double): DoubleArray {
            val p = projector(lambda, phi)
            return doubleArrayOf(p[0] * scale + dx, dy - p[1] * scale)
        }
    }

    // Precision
    private var projectResample = resample(projectTransform, 0.5)

    override var precision = 0.5
        set(value) {
            field = value
            projectResample = resample(projectTransform, value * value)
            reset()
        }

    private val transformRadians = { stream: GeometryStream ->
        object : FilterGeometryStream(stream) {
            override fun point(x: Double, y: Double, z: Double) = stream.point(x.toRadians(), y.toRadians(), z.toRadians())
        }
    }

    private fun transformRotate(rotate: Transform) = { stream: GeometryStream ->
        object : FilterGeometryStream(stream) {
            override fun point(x: Double, y: Double, z: Double) {
                val r = rotate(x, y)
                stream.point(r[0], r[1], 0.0)
            }
        }
    }

    override operator fun invoke(point: DoubleArray): DoubleArray {
        val p = projectRotate(point[0].toRadians(), point[1].toRadians())
        return doubleArrayOf(p[0] * scale + dx, dy - p[1] * scale)
    }

    override fun invert(coordinates: DoubleArray): DoubleArray {
        val pr = projectRotate as? Invertable ?: throw UnsupportedOperationException()

        val p = pr.invert((coordinates[0] - dx) / scale, (dy - coordinates[1]) / scale)
        return doubleArrayOf(p[0].toDegrees(), p[1].toDegrees())
    }

    override fun stream(forStream: GeometryStream): GeometryStream {
        var stream = getCachedStream(forStream)
        if(stream == null) {
            stream = transformRadians(transformRotate(rotator)(preclip(projectResample(postclip(forStream)))))
            cache(forStream, stream)
        }

        return stream
    }

    protected open fun recenter() {

        val lambda = (center.longitude % 360).toRadians()
        val phi = (center.latitude % 360).toRadians()
        val deltaLambda = (rotateX % 360).toRadians()
        val deltaPhi = (rotateY % 360).toRadians()
        val deltaGamma = (rotateZ % 360).toRadians()
        recenter(lambda, phi, deltaLambda, deltaPhi, deltaGamma)
    }

    protected fun recenter(lambda: Double, phi: Double, deltaLambda: Double, deltaPhi: Double, deltaGamma: Double)
    {
        rotator = rotateRadians(deltaLambda, deltaPhi, deltaGamma)
        projectRotate = compose(rotator, projector)
        val center = projector(lambda, phi)
        dx = translateX - center[0] * scale
        dy = translateY + center[1] * scale
        reset()
        invalidate()
    }
}