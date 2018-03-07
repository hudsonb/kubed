package kubed.geo.clip

import kubed.geo.*
import kubed.geo.math.polygonContains
import kubed.math.EPSILON
import kubed.math.HALF_PI
import kubed.util.isTruthy
import java.util.*
import kotlin.Comparator

interface IntersectStream : GeometryStream {
    var clean: Int
}

open class ClipStream(val clip: Clip, val stream: GeometryStream) : MutableGeometryStream() {
    private val line = clip.clipLine(stream)
    private val ringBuffer = Buffer()
    private val ringSink = clip.clipLine(ringBuffer)
    private var polygonStarted = false
    private val polygon: LinkedList<List<DoubleArray>> = LinkedList()
    private val segments: LinkedList<List<List<DoubleArray>>> = LinkedList()
    private var ring: LinkedList<DoubleArray>? = null

    private val compareIntersection = Comparator<Intersection> { i1, i2 ->
        val a = i1.x
        val b = i2.x
        val ca = if(a[0] < 0) a[1] - HALF_PI - EPSILON else HALF_PI - a[1]
        val cb = if(b[0] < 0) b[1] - HALF_PI - EPSILON else HALF_PI - b[1]
        ca.compareTo(cb)
    }

    init {
        point = ::defaultPoint
        lineStart = ::defaultLineStart
        lineEnd = ::defaultLineEnd

        polygonStart = {
            point = ::pointRing
            lineStart = ::ringStart
            lineEnd = ::ringEnd
            segments.clear()
            polygon.clear()
        }

        polygonEnd = {
            point = ::defaultPoint
            lineStart = ::defaultLineStart
            lineEnd = ::defaultLineEnd

            val startInside = polygonContains(polygon, clip.start)

            if(segments.isNotEmpty()) {
                if(!polygonStarted) {
                    stream.polygonStart()
                    polygonStarted = true
                }

                clipRejoin(segments.flatten(), compareIntersection, startInside, clip::interpolate, stream)
            }
            else if(startInside) {
                if(!polygonStarted) {
                    stream.polygonStart()
                    polygonStarted = true
                }
                stream.lineStart()
                clip.interpolate(null, null, 1, stream)
                stream.lineEnd()
            }

            if(polygonStarted) {
                stream.polygonEnd()
                polygonStarted = false
            }

            segments.clear()
            polygon.clear()
        }

        sphere = {
            stream.polygonStart()
            stream.lineStart()
            clip.interpolate(null, null, 1, stream)
            stream.lineEnd()
            stream.polygonEnd()
        }
    }

    private fun defaultPoint(x: Double, y: Double, z: Double) {
        if(clip.isVisible(x, y)) stream.point(x, y, z)
    }

    private fun defaultLineStart() {
        point = ::pointLine
        line.lineStart()
    }

    private fun defaultLineEnd() {
        point = ::defaultPoint
        line.lineEnd()
    }

    private fun pointRing(x: Double, y: Double, z: Double) {
        ring!!.add(doubleArrayOf(x, y))
        ringSink.point(x, y, z)
    }

    private fun pointLine(x: Double, y: Double, z: Double) = line.point(x, y, z)

    private fun ringStart() {
        ringSink.lineStart()
        ring = LinkedList()
    }

    private fun ringEnd() {
        val ring = ring
        if(ring != null) {
            pointRing(ring[0][0], ring[0][1], 0.0)
            ringSink.lineEnd()

            val clean = ringSink.clean
            val ringSegments = LinkedList(ringBuffer.result())

            ring.removeLast()
            polygon.add(ring)
            this.ring = null

            if(ringSegments.isEmpty()) return

            // No intersections
            if((clean and 1).isTruthy()) {
                val segment = ringSegments[0]
                if(segment != null) {
                    val m = segment.lastIndex
                    if(m > 0) {
                        if(!polygonStarted) {
                            stream.polygonStart()
                            polygonStarted = true
                        }
                        stream.lineStart()
                        (0 until m).map { segment[it] }
                                   .forEach { stream.point(it[0], it[1], 0.0) }
                        stream.lineEnd()
                    }
                }
                return
            }

            // Rejoin connected segments
            if(ringSegments.size > 1 && (clean and 2).isTruthy()) {
                val concat = LinkedList(ringSegments.removeLast())
                concat.addAll(ringSegments.removeFirst())
                ringSegments.add(concat)
            }

            segments.add(ringSegments.filter { it.size > 1 })
        }
    }
}