package kubed.geo.projection

import kubed.geo.GeometryStream
import kubed.geo.MultiplexStream
/*
class AlbersUsa : Projection() {
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

    var point: DoubleArray? = null
    val pointStream = object : GeometryStream {
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

    override fun invert(point: DoubleArray): DoubleArray {
        val k = lower48.scale
        val t = lower48.translate
        val x = (point[0] - t[0]) / k
        val y = (point[1] - t[1]) / k

        val p = when {
            y in 0.120..0.234 && x in -0.425..-0.214 -> alaska
            y in 0.166..0.234 && x in -0.214..-0.115 -> hawaii
            else -> lower48
        }

        return p.invert(point)
    }

    override fun stream(stream: GeometryStream): GeometryStream {
        val cs = cache
        return when {
            cs != null && cacheStream == stream -> cs
            else -> {
                cacheStream = stream
                val ms = MultiplexStream(listOf(lower48.stream(stream), alaska.stream(stream), hawaii.stream(stream)))
                cache = ms
                ms
            }
        }
    }
}
        */