package kubed.geo.projection

import kubed.geo.FilterGeometryStream
import kubed.geo.GeoJson
import kubed.geo.GeometryStream
import kubed.geo.clip.clipAntimeridian
import kubed.geo.clip.clipCircle
import kubed.geo.clip.clipRectangle
import kubed.math.toDegrees
import kubed.math.toRadians
import kotlin.math.sqrt

fun projection(projector: Projector) = projection(projector) {}
fun projection(projector: Projector, init: MutableProjection.() -> Unit) = MutableProjection(projector).apply(init)
fun projection(factory: ProjectorFactory) = MutableProjection(factory)
fun projection(factory: ProjectorFactory, init: MutableProjection.() -> Unit) = MutableProjection(factory).apply(init)

interface Projection {
    var precision: Double
    var scale: Double
    var translate: DoubleArray

    operator fun invoke(point: DoubleArray): DoubleArray
    fun invert(coordinates: DoubleArray): DoubleArray
    fun stream(forStream: GeometryStream): GeometryStream

    fun fitExtent(extent: Array<DoubleArray>, geo: GeoJson) = kubed.geo.projection.fitExtent(this, extent, geo)
    fun fitSize(size: DoubleArray, geo: GeoJson) = kubed.geo.projection.fitSize(this, size, geo)
    fun fitWidth(width: Double, geo: GeoJson) = kubed.geo.projection.fitWidth(this, width, geo)
    fun fitHeight(height: Double, geo: GeoJson) = kubed.geo.projection.fitHeight(this, height, geo)
}

abstract class StreamCacheProjection : Projection {
    protected var cache: GeometryStream? = null
    protected var cacheStream: GeometryStream? = null

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
    fun reset(): Projection {
        cache = null
        cacheStream = null
        return this
    }
}

abstract class ClippedProjection : StreamCacheProjection() {
    // Clip Angle
    private var theta = Double.NaN
    var clipAngle: Double
        get() = theta
        set(value) {
            if(value.isNaN()) {
                theta = Double.NaN
                preclip = clipAntimeridian()
            }
            else {
                theta = value.toRadians()
                preclip = clipCircle(theta)
            }
        }

    var preclip: (GeometryStream) -> GeometryStream = clipAntimeridian()
        set(value) {
            theta = Double.NaN
            reset()
            field = value
        }

    // Clip extent
    private val identity = { stream: GeometryStream -> stream }
    private var x0 = 0.0
    private var y0 = 0.0
    private var x1 = 0.0
    private var y1 = 0.0
    var postclip = identity
    open var clipExtent: Array<DoubleArray>?
        get() = if(x0.isNaN()) null else arrayOf(doubleArrayOf(x0, y0), doubleArrayOf(x1, y1))
        set(value) {
            if(value == null) {
                x0 = Double.NaN
                y0 = Double.NaN
                x1 = Double.NaN
                y1 = Double.NaN
                postclip = identity
            }
            else {
                x0 = value[0][0]
                y0 = value[0][1]
                x1 = value[1][0]
                y1 = value[1][1]
                postclip = clipRectangle(x0, y0, x1, y1)
            }

            reset()
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
    private var k = 150.0

    override var scale: Double
        get() = k
        set(value){
            k = value
            recenter()
        }

    // Translate
    private var x = 480.0
    private var y = 250.0
    override var translate: DoubleArray
        get() = doubleArrayOf(x, y)
        set(value) {
            x = value[0]
            y = value[1]
            recenter()
        }

    // Center
    private var dx = 0.0
    private var dy = 0.0
    private var lambda = 0.0
    private var phi = 0.0
    open var center
        get() = doubleArrayOf(lambda.toDegrees(), phi.toDegrees())
        set(value) {
            lambda = (value[0] % 360).toRadians()
            phi = (value[1] % 360).toRadians()
            recenter()
        }

    // Rotate
    private var deltaLambda = 0.0
    private var deltaPhi = 0.0
    private var deltaGamma = 0.0
    private lateinit var rotator: Transform

    open var rotate: DoubleArray
        get() = doubleArrayOf(deltaLambda.toDegrees(), deltaPhi.toDegrees(), deltaGamma.toDegrees())
        set(value) {
            deltaLambda = (value[0] % 360).toRadians()
            deltaPhi = (value[1] % 360).toRadians()
            deltaGamma = if(value.size > 2) (value[2] % 360).toRadians() else 0.0
            recenter()
        }

    private lateinit var projectRotate: Transform

    private val projectTransform = object : Transform {
        override fun invoke(lambda: Double, phi: Double): DoubleArray {
            val p = project(lambda, phi)
            return doubleArrayOf(p[0] * k + dx, dy - p[1] * k)
        }
    }

    // Precision
    private var delta2 = 0.5
    private var projectResample = resample(projectTransform, delta2)
    override var precision: Double
        get() = sqrt(delta2)
        set(value) {
            delta2 = value * value
            projectResample = resample(projectTransform, delta2)
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
        return doubleArrayOf(p[0] * k + dx, dy - p[1] * k)
    }

    override fun invert(coordinates: DoubleArray): DoubleArray {
        val pr = projectRotate as? Invertable ?: throw UnsupportedOperationException()

        val p = pr.invert((coordinates[0] - dx) / k, (dy - coordinates[1]) / k)
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

    fun recenter(): Projection {
        rotator = rotateRadians(deltaLambda, deltaPhi, deltaGamma)
        projectRotate = compose(rotator, project)
        val center = project(lambda, phi)
        dx = x - center[0] * k
        dy = y + center[1] * k
        return reset()
    }
}