package kubed.geo.clip

import kubed.geo.Buffer
import kubed.geo.GeometryStream
import kubed.math.EPSILON
import kubed.util.isTruthy
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

const val CLIP_MAX = 1e9
const val CLIP_MIN = -CLIP_MAX

fun clipRectangle(x0: Double, y0: Double, x1: Double, y1: Double) = RectangleClip(x0, y0, x1, y1)::clipLine

class RectangleClip(val x0: Double, val y0: Double, val x1: Double, val y1: Double) : Clip {
    override var start = doubleArrayOf(0.0, 0.0) // Unused

    override fun isVisible(x: Double, y: Double) = x in x0..x1 && y in y0..y1

    override fun clipLine(stream: GeometryStream): IntersectStream {

        return object : IntersectStream {
            override var clean: Int = 0

            private var streamingLine = false
            private var activeStream = stream
            private val bufferStream = Buffer()
            private var x__ = Double.NaN
            private var y__ = Double.NaN
            private var v__ = false
            private var x_ = Double.NaN
            private var y_ = Double.NaN
            private var v_ = false
            private var segments: ArrayList<List<List<DoubleArray>>>? = null
            private var ring: ArrayList<DoubleArray>? = null
            private var polygon: ArrayList<List<DoubleArray>>? = null
            private var first = false

            override fun point(x: Double, y: Double, z: Double) {
                if(streamingLine) linePoint(x, y)
                else if(isVisible(x, y)) activeStream.point(x, y, 0.0)
            }

            override fun lineStart() {
                streamingLine = true

                val poly = polygon
                if(poly != null) {
                    val r = ArrayList<DoubleArray>()
                    ring = r
                    poly.add(r)
                }

                first = true
                v_ = false
                x_ = Double.NaN
                y_ = Double.NaN
            }

            override fun lineEnd() {
                if(segments != null) {
                    linePoint(x__, y__)
                    if (v__ && v_) bufferStream.rejoin()
                    segments!!.add(bufferStream.result())
                }
                streamingLine = false
                if(v_) activeStream.lineEnd()
            }

            override fun polygonStart() {
                activeStream = bufferStream
                segments = ArrayList()
                polygon = ArrayList()
                clean = 1
            }

            override fun polygonEnd() {
                val startInside = polygonInside().isTruthy()
                val cleanInside = clean.isTruthy() && startInside
                val flattenedSegments = segments?.flatten() ?: emptyList()
                val visible = flattenedSegments.isNotEmpty()

                if(cleanInside || visible) {
                    stream.polygonStart()
                    if(cleanInside) {
                        stream.lineStart()
                        interpolate(null, null, 1, stream)
                        stream.lineEnd()
                    }
                    if(visible) clipRejoin(flattenedSegments, Comparator { o1, o2 -> comparePoint(o1.x, o2.x) }, startInside, ::interpolate, stream)
                    stream.polygonEnd()
                }
                activeStream = stream
                segments = null
                polygon = null
                ring = null
            }

            private fun linePoint(lambda: Double, phi: Double) {
                var x = lambda
                var y = phi

                val v = isVisible(x, y)
                if(polygon != null) ring?.add(doubleArrayOf(x, y))
                if(first) {
                    x__ = x
                    y__ = y
                    v__ = v
                    first = false
                    if(v) {
                        activeStream.lineStart()
                        activeStream.point(x, y, 0.0)
                    }
                }
                else {
                    if(v && v_) activeStream.point(x, y, 0.0)
                    else {
                        x_ = max(CLIP_MIN, min(CLIP_MAX, x_))
                        y_ = max(CLIP_MIN, min(CLIP_MAX, y_))
                        x = max(CLIP_MIN, min(CLIP_MAX, x))
                        y = max(CLIP_MIN, min(CLIP_MAX, y))
                        val a = doubleArrayOf(x_, y_)
                        val b = doubleArrayOf(x, y)

                        if(clipLine(a, b, x0, y0, x1, y1)) {
                            if(!v_) {
                                activeStream.lineStart()
                                activeStream.point(a[0], a[1], 0.0)
                            }
                            activeStream.point(b[0], b[1], 0.0)
                            if(!v) activeStream.lineEnd()
                            clean = 0
                        }
                        else if(v) {
                            activeStream.lineStart()
                            activeStream.point(x, y, 0.0)
                            clean = 0
                        }
                    }
                }

                x_ = x
                y_ = y
                v_ = v
            }

            private fun polygonInside(): Int {
                var winding = 0

                var ring: List<DoubleArray>
                var j: Int
                var m: Int
                var point: DoubleArray
                var a0: Double
                var a1: Double
                var b0: Double
                var b1: Double

                val poly = polygon ?: throw IllegalStateException()
                for(i in poly.indices) {
                    ring = poly[i]
                    j = 1
                    m = ring.size
                    point = ring[0]
                    b0 = point[0]
                    b1 = point[1]
                    while(j < m) {
                        a0 = b0
                        a1 = b1
                        point = ring[j]
                        b0 = point[0]
                        b1 = point[1]
                        if(a1 <= y1) { if(b1 > y1 && (b0 - a0) * (y1 - a1) > (b1 - a1) * (x0 - a0)) ++winding; }
                        else if(b1 <= y1 && (b0 - a0) * (y1 - a1) < (b1 - a1) * (x0 - a0)) --winding
                        ++j
                    }
                }

                return winding
            }
        }
    }

    override fun interpolate(from: DoubleArray?, to: DoubleArray?, direction: Int, stream: GeometryStream) {
        var a = if(from == null) 0 else corner(from, direction)
        val a1 = if(from == null) 0 else corner(to, direction)

        if(from == null || a != a1 || to != null && (comparePoint(from, to) < 0) xor (direction > 0)) {
            do {
                stream.point(if(a == 0 || a == 3) x0 else x1,
                        if(a > 1) y1 else y0,
                        0.0)
                a = (a + direction + 4) % 4
            } while(a != a1)
        }
        else if(to != null) stream.point(to[0], to[1], 0.0)
    }

    private fun corner(p: DoubleArray?, direction: Int) = when {
        direction > 0 -> when {
            p == null -> 3
            abs(p[0] - x0) < EPSILON -> 0
            abs(p[0] - x1) < EPSILON -> 2
            abs(p[1] - y1) < EPSILON -> 1
            else -> 3
        }
        else -> when {
            p == null -> 2
            abs(p[0] - x0) < EPSILON -> 3
            abs(p[0] - x1) < EPSILON -> 1
            abs(p[1] - y1) < EPSILON -> 0
            else -> 2
        }
    }

    private fun comparePoint(a: DoubleArray, b: DoubleArray): Int {
        val ca = corner(a, 1)
        val cb = corner(b, 1)

        return when {
            ca != cb -> ca.compareTo(cb)
            ca == 0 -> b[1].compareTo(a[1])
            ca == 1 -> a[0].compareTo(b[0])
            ca == 2 -> a[1].compareTo(b[1])
            else -> b[0].compareTo(a[0])
        }
    }
}