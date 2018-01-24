//package kubed.geo.clip
//
//import kubed.geo.Buffer
//import kubed.geo.Stream
//import kubed.geo.point
//import kubed.util.MoreMath
//import kubed.util.isTruthy
//import kotlin.math.abs
//import kotlin.math.max
//import kotlin.math.min
//
//const val clipMax = 1e9
//const val clipMin = -clipMax
//
//class Extent(val x0: Double, val y0: Double, val x1: Double, val y1: Double) : Clip {
//    override fun pointVisible(point: DoubleArray) = visible(point[0], point[1])
//
//    override fun interpolate(from: DoubleArray?, to: DoubleArray?, direction: Int, stream: Stream) {
//        if(to == null) return
//
//        var a = if(from == null) 0 else corner(from, direction)
//        val a1 = if(from == null) 0 else corner(to, direction)
//
//        if(from == null || a != a1 || (comparePoint(from, to) < 0) xor (direction > 0)) {
//            do {
//                stream.point(if(a == 0 || a == 3) x0 else x1,
//                             if(a > 1) y1 else y0)
//                a = (a + direction + 4) % 4
//            } while(a != a1)
//        }
//        else stream.point(to[0], to[1])
//    }
//
//    override fun clipLine(stream: Stream): Stream {
//        var activeStream = stream
//        val bufferStream = Buffer()
//        var x__ = Double.NaN
//        var y__ = Double.NaN
//        var v__ = false
//        var x_ = Double.NaN
//        var y_ = Double.NaN
//        var v_ = false
//        var clean = true
//        var segments: ArrayList<List<List<DoubleArray>>>? = null
//        var flattenedSegments: ArrayList<List<DoubleArray>>?
//        var ring: ArrayList<DoubleArray>? = null
//        var polygon: ArrayList<List<DoubleArray>>? = null
//        var first = false
//
//        return object : Stream() {
//            init {
//                point = ::point
//                lineStart = ::lineStart
//                lineEnd = ::lineEnd
//                polygonStart = ::polygonStart
//                polygonEnd = ::polygonEnd
//            }
//
//            private fun point(x: Double, y: Double) {
//                if(visible(x, y)) activeStream.point(x, y)
//            }
//
//            private fun lineStart() {
//                point = ::linePoint
//
//                val poly = polygon
//                if(poly != null) {
//                    val r = ArrayList<DoubleArray>()
//                    ring = r
//                    poly.add(r)
//                }
//
//                first = true
//                v_ = false
//                x_ = Double.NaN
//                y_ = Double.NaN
//            }
//
//            private fun lineEnd() {
//                val segs = segments
//                if(segs != null) {
//                    linePoint(x__, y__)
//                    if(v__ && v_) bufferStream.rejoin()
//                    segs.add(bufferStream.result())
//                }
//                point = ::point
//                if(v_) activeStream.lineEnd()
//            }
//
//            private fun polygonStart() {
//                activeStream = bufferStream
//                segments = ArrayList()
//                flattenedSegments = ArrayList()
//                polygon = ArrayList()
//                clean = true
//            }
//
//            private fun polygonEnd() {
//                val startInside = polygonInside() == 0
//                val cleanInside = clean && startInside
//                flattenedSegments = ArrayList(segments?.flatten())
//                val visible = flattenedSegments!!.isNotEmpty()
//
//                if(cleanInside || visible) {
//                    stream.polygonStart()
//                    if(cleanInside) {
//                        interpolate(null, null, 1, stream)
//                        stream.lineEnd()
//                    }
//                    if(visible) clipRejoin(flattenedSegments!!, Comparator { o1, o2 -> comparePoint(o1.x, o2.x) }, startInside, ::interpolate, stream)
//                    stream.polygonEnd()
//                }
//                activeStream = stream
//                segments = null
//                polygon = null
//                ring = null
//            }
//
//            private fun linePoint(lambda: Double, phi: Double) {
//                var x = lambda
//                var y = phi
//
//                val v = visible(x, y)
//                if(polygon != null) ring.add(doubleArrayOf(x, y))
//                if(first) {
//                    x__ = x
//                    y__ = y
//                    v__ = v
//                    first = false
//                    if(v) {
//                        activeStream.lineStart()
//                        activeStream.point(x, y)
//                    }
//                }
//                else {
//                    if(v && v_) activeStream.point(x, y)
//                    else {
//                        x_ = max(clipMin, min(clipMax, x_))
//                        y_ = max(clipMin, min(clipMax, y_))
//                        x = max(clipMin, min(clipMax, x))
//                        y = max(clipMin, min(clipMax, y))
//                        val a = doubleArrayOf(x_, y_)
//                        val b = doubleArrayOf(x, y)
//
//                        if(clipLine(a, b, x0, y0, x1, y1)) {
//                            if(!v_) {
//                                activeStream.lineStart()
//                                activeStream.point(a[0], a[1])
//                            }
//                            activeStream.point(b[0], b[1])
//                            if(!v) activeStream.lineEnd()
//                            clean = false
//                        }
//                        else if(v) {
//                            activeStream.lineStart()
//                            activeStream.point(x, y)
//                            clean = false
//                        }
//                    }
//                }
//
//                x_ = x
//                y_ = y
//                v_ = v
//            }
//
//            private fun polygonInside(): Int {
//                var winding = 0
//
//                var ring: List<DoubleArray>
//                var j: Int
//                var m: Int
//                var point: DoubleArray
//                var a0: Double
//                var a1: Double
//                var b0: Double
//                var b1: Double
//
//                val poly = polygon ?: throw IllegalStateException()
//                for(i in poly.indices) {
//                    ring = poly[i]
//                    j = 1
//                    m = ring.size
//                    point = ring[0]
//                    b0 = point[0]
//                    b1 = point[1]
//                    while(j < m) {
//                        a0 = b0
//                        a1 = b1
//                        point = ring[j]
//                        b0 = point[0]
//                        b1 = point[1]
//                        if(a1 <= y1) { if(b1 > y1 && (b0 - a0) * (y1 - a1) > (b1 - a1) * (x0 - a0)) ++winding; }
//                        else if(b1 <= y1 && (b0 - a0) * (y1 - a1) < (b1 - a1) * (x0 - a0)) --winding
//                        ++j
//                    }
//                }
//
//                return winding
//            }
//        }
//    }
//
//
//    override var start: DoubleArray
//        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
//        set(value) {}
//
//    private fun visible(x: Double, y: Double) =  x in x0..x1 && y in y0..y1
//
//    private fun corner(p: DoubleArray, direction: Int) = when {
//        direction > 0 -> when {
//            abs(p[0] - x0) < MoreMath.EPSILON -> 0
//            abs(p[0] - x1) < MoreMath.EPSILON -> 2
//            abs(p[1] - y1) < MoreMath.EPSILON -> 1
//            else -> 3
//        }
//        else -> when {
//            abs(p[0] - x0) < MoreMath.EPSILON -> 3
//            abs(p[0] - x1) < MoreMath.EPSILON -> 1
//            abs(p[1] - y1) < MoreMath.EPSILON -> 0
//            else -> 2
//        }
//    }
//
//    private fun comparePoint(a: DoubleArray, b: DoubleArray): Int {
//        val ca = corner(a, 1)
//        val cb = corner(b, 1)
//
//        return when(ca) {
//            cb -> ca - cb
//            0 -> (b[1] - a[1]).toInt()
//            1 -> (a[0] - b[0]).toInt()
//            2 -> (a[1] - b[1]).toInt()
//            else -> (b[0] - a[0]).toInt()
//        }
//    }
//}