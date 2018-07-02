package kubed.geo.projection

import javafx.geometry.Rectangle2D
import kubed.geo.GeometryStream
import kubed.geo.MultiplexStream
import kubed.geo.Position
import kubed.math.EPSILON

fun albersUsa() = albersUsa {}
fun albersUsa(init: AlbersUsa.() -> Unit) = AlbersUsa().apply {
    scale = 1070.0
    init()
}

class AlbersUsa : StreamCacheProjection() {
    private val lower48 = albers()
    lateinit var lower48Point: GeometryStream

    private val alaska = conicEqualArea {
        rotateX = 154.0
        center = Position(-2.0, 58.5)
        parallels = doubleArrayOf(55.0, 65.0)
        scale = lower48.scale * .35
    }
    lateinit var alaskaPoint: GeometryStream

    private val hawaii = conicEqualArea {
        rotateX = 157.0
        center = Position(-3.0, 19.9)
        parallels = doubleArrayOf(8.0, 18.0)
        scale = lower48.scale
    }
    lateinit var hawaiiPoint: GeometryStream

    override var precision: Double
        get() = lower48.precision
        set(value) {
            lower48.precision = value
            alaska.precision = value
            hawaii.precision = value
        }

    override var scale: Double
        get() = lower48.scale
        set(k) {
            lower48.scale = k
            alaska.scale = k * .35
            hawaii.scale = k
        }

    override var translateX: Double
        get() = lower48.translateX
        set(x) {
            lower48.translateX = x
            update()
        }

    override var translateY: Double
        get() = lower48.translateY
        set(y) {
            lower48.translateY = y
            update()
        }

    private var point: DoubleArray? = null
    private val pointStream = object : GeometryStream {
        override fun point(x: Double, y: Double, z: Double) {
            point = doubleArrayOf(x, y)
        }
    }

    init {
        update()
    }

    private fun update() {
        val k = scale
        val x = translateX
        val y = translateY

        with(lower48) {
            clipExtent = Rectangle2D(x - 0.455 * k, y - 0.238 * k, 0.91 * k, 0.476 * k)
            lower48Point = stream(pointStream)
        }

        with(alaska) {
            translateX = x - 0.307 * k
            translateY = y + 0.201 * k
            clipExtent = Rectangle2D(x - 0.425 * k + EPSILON, y + 0.120 * k + EPSILON,
                    0.211 * k - EPSILON * 2, 0.354 * k - EPSILON * 2)
            alaskaPoint = stream(pointStream)
        }

        with(hawaii) {
            translateX = x - 0.205 * k
            translateY = y + 0.212 * k
            clipExtent = Rectangle2D(x - 0.214 * k + EPSILON, y + 0.166 * k + EPSILON,
                                 0.099 * k - EPSILON * 2, 0.068 * k - EPSILON * 2)
            hawaiiPoint = stream(pointStream)
        }

        reset()
        invalidate()
    }

    override fun invoke(point: DoubleArray): DoubleArray {
        val x = point[0]
        val y = point[1]

        this.point = null

        lower48Point.point(x, y, 0.0)
        val lp = this.point
        if(lp != null) return lp

        alaskaPoint.point(x, y, 0.0)
        val ap = this.point
        if(ap != null) return ap

        hawaiiPoint.point(x, y, 0.0)
        val hp = this.point
        if(hp != null) return hp

        throw Exception("Bug in AlbersUsa.invoke")
    }

    override fun invert(coordinates: DoubleArray): DoubleArray {
        val x = (coordinates[0] - translateX) / scale
        val y = (coordinates[1] - translateY) / scale

        val p = when {
            y in 0.120..0.234 && x in -0.425..-0.214 -> alaska
            y in 0.166..0.234 && x in -0.214..-0.115 -> hawaii
            else -> lower48
        }

        return p.invert(coordinates)
    }

    override fun stream(forStream: GeometryStream): GeometryStream {
        var stream = getCachedStream(forStream)
        if(stream == null) {
            stream = MultiplexStream(listOf(lower48.stream(forStream), alaska.stream(forStream), hawaii.stream(forStream)))
            cache(forStream, stream)
        }

        return stream
    }
}