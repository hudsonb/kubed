package kubed.geo.projection

import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
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
fun projection(factory: ProjectorFactory) = MutableProjection(factory)
fun projection(factory: ProjectorFactory, init: MutableProjection.() -> Unit) = MutableProjection(factory).apply(init)

interface ProjectionListener {
    fun projectionChanged(projection: Projection)
}

interface Projection {
    val precisionProperty: DoubleProperty
    var precision: Double

    val scaleProperty: DoubleProperty
    var scale: Double

    val translateXProperty: DoubleProperty
    var translateX: Double

    val translateYProperty: DoubleProperty
    var translateY: Double

    operator fun invoke(point: DoubleArray): DoubleArray
    fun invert(coordinates: DoubleArray): DoubleArray
    fun stream(forStream: GeometryStream): GeometryStream

    fun fitExtent(extent: Array<DoubleArray>, geo: GeoJson) = kubed.geo.projection.fitExtent(this, extent, geo)
    fun fitSize(size: DoubleArray, geo: GeoJson) = kubed.geo.projection.fitSize(this, size, geo)
    fun fitWidth(width: Double, geo: GeoJson) = kubed.geo.projection.fitWidth(this, width, geo)
    fun fitHeight(height: Double, geo: GeoJson) = kubed.geo.projection.fitHeight(this, height, geo)

    fun invalidate() {}

    fun addListener(listener: ProjectionListener)
    fun removeListener(listener: ProjectionListener): Boolean
}

abstract class AbstractProjection : Projection {
    private var invalidated = false

    private val listeners = CopyOnWriteArrayList<ProjectionListener>()

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
     * Adds the given [ProjectionListener] to this projection. [ProjectionListener.projectionChanged] is called only once
     * per frame, and is guaranteed to be called from the JavaFX thread.
     */
    override fun addListener(listener: ProjectionListener) {
        if(!listeners.contains(listener)) listeners.add(listener)
    }

    /**
     * Removes the given [ProjectionListener] from this projection.
     */
    override fun removeListener(listener: ProjectionListener): Boolean = listeners.remove(listener)

    private fun fireProjectionChanged() = ArrayList(listeners).forEach { it.projectionChanged(this) }
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
    val clipAngleProperty = SimpleDoubleProperty(Double.NaN)
    var clipAngle: Double
        get() = clipAngleProperty.get()
        set(theta) = clipAngleProperty.set(theta)


    protected var preclip: (GeometryStream) -> GeometryStream = clipAntimeridian()
        set(value) {
            field = value
            reset()
        }

    // Clip extent
    private val identity = { stream: GeometryStream -> stream }
    protected var postclip = identity
    var clipExtentProperty = SimpleObjectProperty<Rectangle2D?>(null)
    var clipExtent: Rectangle2D?
        get() = clipExtentProperty.get()
        set(value) = clipExtentProperty.set(value)

    init {
        clipAngleProperty.addListener { _ ->
            preclip = if(clipAngle.isNaN()) clipAntimeridian()
            else clipCircle(clipAngle.toRadians())

            invalidate()
        }

        clipExtentProperty.addListener { _ ->
            val extent = clipExtent
            postclip = if(extent == null) identity
                       else clipRectangle(extent.minX, extent.minY, extent.maxX, extent.maxY)

            reset()
            invalidate()
        }
    }
}

open class MutableProjection(protected val factory: ProjectorFactory): ClippedProjection() {
    constructor(projector: Projector) : this(object : ProjectorFactory {
        override fun create(): Projector {
            return projector
        }
    })

    protected var project: Projector = factory.create()

    // Scale
    final override val scaleProperty = SimpleDoubleProperty(150.0)
    override var scale: Double
        get() = scaleProperty.get()
        set(value) = scaleProperty.set(value)

    // Translate
    final override val translateXProperty = SimpleDoubleProperty(0.0)
    override var translateX: Double
        get() = translateXProperty.get()
        set(x) = translateXProperty.set(x)

    final override val translateYProperty = SimpleDoubleProperty(0.0)
    override var translateY: Double
        get() = translateYProperty.get()
        set(y) = translateYProperty.set(y)

    // Center
    private var dx = 0.0
    private var dy = 0.0

    val centerProperty = SimpleObjectProperty<Position>(Position(0.0, 0.0))
    var center: Position
        get() = centerProperty.get()
        set(p) = centerProperty.set(p)

    // Rotate
    private lateinit var rotator: Transform

    val rotateXProperty = SimpleDoubleProperty(0.0)
    var rotateX: Double
        get() = rotateXProperty.get()
        set(x) = rotateXProperty.set(x)

    val rotateYProperty = SimpleDoubleProperty(0.0)
    var rotateY: Double
        get() = rotateYProperty.get()
        set(y) = rotateYProperty.set(y)

    val rotateZProperty = SimpleDoubleProperty(0.0)
    var rotateZ: Double
        get() = rotateZProperty.get()
        set(z) = rotateZProperty.set(z)

    private lateinit var projectRotate: Transform

    private val projectTransform = object : Transform {
        override fun invoke(lambda: Double, phi: Double): DoubleArray {
            val p = project(lambda, phi)
            return doubleArrayOf(p[0] * scale + dx, dy - p[1] * scale)
        }
    }

    // Precision
    private var projectResample = resample(projectTransform, 0.5)

    final override val precisionProperty = SimpleDoubleProperty(0.5)
    override var precision: Double
        get() = precisionProperty.get()
        set(value) {
            precisionProperty.set(value)
        }

    private val transformRadians = { stream: GeometryStream ->
        object : FilterGeometryStream(stream) {
            override fun point(x: Double, y: Double, z: Double) = stream.point(x.toRadians(), y.toRadians(), z.toRadians())
        }
    }

    init {
        precisionProperty.addListener { _ ->
            projectResample = resample(projectTransform, precision * precision)
            reset()
        }

        translateXProperty.addListener { _ -> recenter(); }
        translateYProperty.addListener { _ -> recenter(); }
        scaleProperty.addListener { _ -> recenter(); }
        centerProperty.addListener { _ -> recenter() }
        rotateXProperty.addListener { _ -> recenter() }
        rotateYProperty.addListener { _ -> recenter() }
        rotateZProperty.addListener { _ -> recenter() }
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
            cache(stream, stream)
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
        projectRotate = compose(rotator, project)
        val center = project(lambda, phi)
        dx = translateX - center[0] * scale
        dy = translateY + center[1] * scale
        reset()
        invalidate()
    }
}