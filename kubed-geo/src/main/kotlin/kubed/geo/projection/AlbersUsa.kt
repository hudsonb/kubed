package kubed.geo.projection

import kubed.geo.GeometryStream
import kubed.geo.MultiplexStream
import kubed.math.EPSILON

fun albersUsa() = albersUsa {}
fun albersUsa(init: AlbersUsa.() -> Unit) = AlbersUsa().apply {
    scale = 1070.0
    init()
}

class AlbersUsa : StreamCacheProjection() {
    val lower48 = albers()
    lateinit var lower48Point: GeometryStream

    val alaska = conicEqualArea {
        rotate = doubleArrayOf(154.0, 0.0)
        center = doubleArrayOf(-2.0, 58.5)
        parallels = doubleArrayOf(55.0, 65.0)
    }
    lateinit var alaskaPoint: GeometryStream

    val hawaii = conicEqualArea {
        rotate = doubleArrayOf(157.0, 0.0)
        center = doubleArrayOf(-3.0, 19.9)
        parallels = doubleArrayOf(8.0, 18.0)
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
        set(value) {
            lower48.scale = value
            alaska.scale = value * 0.35
            hawaii.scale = value
            translate = lower48.translate
        }

    override var translate: DoubleArray
        get() = lower48.translate
        set(value) {
            val k = lower48.scale
            val x = value[0]
            val y = value[1]

            with(lower48) {
                translate = value
                clipExtent = arrayOf(doubleArrayOf(x - 0.455 * k, y - 0.238 * k),
                                     doubleArrayOf(x + 0.455 * k, y + 0.238 * k))
            }
            lower48Point = lower48.stream(pointStream)

            with(alaska) {
                translate = doubleArrayOf(x - 0.307 * k, y + 0.201 * k)
                clipExtent = arrayOf(doubleArrayOf(x - 0.425 * k + EPSILON, y + 0.120 * k + EPSILON),
                                     doubleArrayOf(x - 0.214 * k - EPSILON, y + 0.234 * k - EPSILON))
            }
            alaskaPoint = alaska.stream(pointStream)

            with(hawaii) {
                translate = doubleArrayOf(x - 0.205 * k, y + 0.212 * k)
                clipExtent = arrayOf(doubleArrayOf(x - 0.214 * k + EPSILON, y + 0.166 * k + EPSILON),
                                     doubleArrayOf(x - 0.115 * k - EPSILON, y + 0.234 * k - EPSILON))
            }
            hawaiiPoint = hawaii.stream(pointStream)

            reset()
        }

    private var point: DoubleArray? = null
    private val pointStream = object : GeometryStream {
        override fun point(x: Double, y: Double, z: Double) {
            point = doubleArrayOf(x, y)
        }
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
        val k = lower48.scale
        val t = lower48.translate
        val x = (coordinates[0] - t[0]) / k
        val y = (coordinates[1] - t[1]) / k

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