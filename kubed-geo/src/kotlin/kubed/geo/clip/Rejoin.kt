package kubed.geo.clip

import kubed.geo.GeometryStream
import kubed.geo.pointsEqual
import java.util.*

data class Intersection(val x: DoubleArray,
                        val z: List<DoubleArray>?,
                        var o: Intersection?,
                        var e: Boolean,
                        var v: Boolean = false,
                        var n: Intersection? = null,
                        var p: Intersection? = null) {
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other !is Intersection) return false

        if(!Arrays.equals(x, other.x)) return false
        if(z != other.z) return false
        if(o != other.o) return false
        if(e != other.e) return false
        if(v != other.v) return false
        if(n != other.n) return false
        if(p != other.p) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(x)
        result = 31 * result + (z?.hashCode() ?: 0)
        result = 31 * result + (o?.hashCode() ?: 0)
        result = 31 * result + e.hashCode()
        result = 31 * result + v.hashCode()
        result = 31 * result + (n?.hashCode() ?: 0)
        result = 31 * result + (p?.hashCode() ?: 0)
        return result
    }
}

fun clipRejoin(segments: List<List<DoubleArray>>, compareIntersection: Comparator<Intersection>,
               startInside: Boolean, interpolate: (DoubleArray, DoubleArray, Int, GeometryStream) -> Unit, stream: GeometryStream) {
    val subject = ArrayList<Intersection>()
    val clip = ArrayList<Intersection>()

    segments.forEach { segment ->
        val n = segment.size - 1
        if(n <= 0) return

        var p0 = segment.first()
        val p1 = segment.last()

        // If the first and last points of a segment are coincident, then treat as a
        // closed ring. TODO if all rings are closed, then the winding order of the
        // exterior ring should be checked.
        if(pointsEqual(p0, p1)) {
            stream.lineStart()
            var i = 0
            while (i < n) {
                p0 = segment[i]
                stream.point(p0[0], p0[1], 0.0)
                ++i
            }
            stream.lineEnd()
            return
        }

        var x = Intersection(p0, segment, null, true)
        subject += x

        var o = Intersection(p0, null, x, false)
        x.o = o
        clip += o

        x = Intersection(p1, segment, null, false)
        subject += x
        o = Intersection(p1, null, x, true)
        x.o = o
        clip += o
    }

    if(subject.isEmpty()) return

    clip.sortWith(compareIntersection)
    link(subject)
    link(clip)

    var si = startInside
    for(i in clip.indices) {
        si = !si
        clip[i].e = si
    }

    val start = subject.first()
    var points: List<DoubleArray>

    while(true) {
        var current: Intersection? = start
        var isSubject = true

        while(current?.v == true) {
            current = current.n
            if(current == start) return
        }

        points = current?.z ?: emptyList()
        stream.lineStart()
        do {
            current?.o?.v = true
            current?.v = true

            if(current?.e == true) {
                if(isSubject) for(point in points) stream.point(point[0], point[1], 0.0)
                else interpolate(current.x, current.n!!.x, 1, stream)
                current = current.n
            }
            else {
                if(isSubject) {
                    points = current?.p?.z ?: emptyList()
                    for(point in points.asReversed()) stream.point(point[0], point[1], 0.0)
                }
                else interpolate(current?.x ?: doubleArrayOf(), current?.p?.x ?: doubleArrayOf() , -1, stream)
                current = current?.p
            }
            current = current?.o
            points = current?.z ?: emptyList()
            isSubject = !isSubject
        } while(current?.v == false)
        stream.lineEnd()
    }
}

private fun link(list: List<Intersection>) {
    if(list.isEmpty()) return

    var a = list.first()
    var b: Intersection
    var i = 0
    while(++i < list.size) {
        b = list[i]
        a.n = b
        b.p = a
        a = b
    }

    b = list.first()
    a.n = b
    b.p = a
}