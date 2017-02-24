package kubed.shape

import javafx.geometry.Point2D
import kubed.path.Context
import kubed.path.PathContext
import kubed.util.MoreMath
import kubed.util.isTruthy

class Arc<T> : PathShape<Arc<T>, T>() {
    var innerRadius: (T) -> Double = { 0.0 }
    var outerRadius: (T) -> Double = { throw IllegalStateException("outerRadius must be specified") }
    var startAngle: (T) -> Double = { throw IllegalStateException("startAngle must be specified") }
    var endAngle: (T) -> Double = { throw IllegalStateException("endAngle must be specified") }
    var padAngle: (T) -> Double = { 0.0 }
    var cornerRadius: (T) -> Double = { 0.0 }
    var padRadius: ((T) -> Double)? = null

    fun innerRadius(value: Double) = innerRadius { value }
    fun innerRadius(f: (T) -> Double): Arc<T> {
        innerRadius = f
        return this
    }

    fun outerRadius(value: Double) = outerRadius { value }
    fun outerRadius(f: (T) -> Double): Arc<T> {
        outerRadius = f
        return this
    }

    fun startAngle(value: Double) = startAngle { value }
    fun startAngle(f: (T) -> Double): Arc<T> {
        startAngle = f
        return this
    }

    fun endAngle(value: Double) = endAngle { value }
    fun endAngle(f: (T) -> Double): Arc<T> {
        endAngle = f
        return this
    }

    fun padAngle(value: Double) = padAngle { value }
    fun padAngle(f: (T) -> Double): Arc<T> {
        padAngle = f
        return this
    }

    fun padRadius(value: Double) = padRadius { value }
    fun padRadius(f: ((T) -> Double)?): Arc<T> {
        padRadius = f
        return this
    }

    fun cornerRadius(value: Double) = cornerRadius { value }
    fun cornerRadius(f: (T) -> Double): Arc<T> {
        cornerRadius = f
        return this
    }

    fun centroid(d: T): Point2D {
        val r = (innerRadius(d) + outerRadius(d)) / 2
        val a = (startAngle(d) + endAngle(d)) / 2.0 - Math.PI / 2.0
        return Point2D(Math.cos(a) * r, Math.sin(a) * r)
    }

    override fun generate(d: T): Context {
        val context = PathContext()
        var r0 = innerRadius(d)
        var r1 = outerRadius(d)
        val a0 = startAngle(d) - MoreMath.HALF_PI
        val a1 = endAngle(d) - MoreMath.HALF_PI
        val da = Math.abs(a1 - a0)
        val cw = a1 > a0

        // Ensure that the outer radius is always larger than the inner radius.
        if(r1 < r0) {
            val r = r1
            r1 = r0
            r0 = r
        }

        if(r1 <= MoreMath.EPSILON) // Is it a point?
            context.moveTo(0.0, 0.0)
        else if(da > MoreMath.TAU - MoreMath.EPSILON) { // Or is it a circle or annulus?
            context.moveTo(r1 * Math.cos(a0), r1 * Math.sin(a0))
            context.arc(0.0, 0.0, r1, a0, a1, !cw)
            if(r0 > MoreMath.EPSILON) {
                context.moveTo(r0 * Math.cos(a1), r0 * Math.sin(a1))
                context.arc(0.0, 0.0, r0, a1, a0, cw);
            }
        }
        else { // Or is it a circular or annular sector?
            var a01 = a0
            var a11 = a1
            var a00 = a0
            var a10 = a1
            var da0 = da
            var da1 = da
            val ap = padAngle(d) / 2
            val rp = if(ap > MoreMath.EPSILON && padRadius != null) padRadius!!.invoke(d) else Math.sqrt(r0 * r0 + r1 * r1)
            val rc = Math.min(Math.abs(r1 - r0) / 2, cornerRadius(d))
            var rc0 = rc
            var rc1 = rc

            // Apply padding? Note that since r1 ≥ r0, da1 ≥ da0.
            if(rp > MoreMath.EPSILON) {
                var p0 = asin(rp / r0 * Math.sin(ap))
                var p1 = asin(rp / r1 * Math.sin(ap))

                da0 -= p0 * 2
                if(da0 > MoreMath.EPSILON) {
                    p0 *= if(cw) 1 else -1
                    a00 += p0
                    a10 -= p0
                }
                else {
                    da0 = 0.0
                    a00 = (a0 + a1) / 2
                    a10 = a00
                }

                da1 -= p1 * 2
                if(da1 > MoreMath.EPSILON) {
                    p1 *= if(cw) 1 else -1
                    a01 += p1
                    a11 -= p1
                }
                else {
                    da1 = 0.0
                    a01 = (a0 + a1) / 2
                    a11 = a01
                }
            }

            val x00 = r0 * Math.cos(a00)
            val y00 = r0 * Math.sin(a00)
            val x01 = r1 * Math.cos(a01)
            val y01 = r1 * Math.sin(a01)
            val x10 = r0 * Math.cos(a10)
            val y10 = r0 * Math.sin(a10)
            val x11 = r1 * Math.cos(a11)
            val y11 = r1 * Math.sin(a11)

            // Apply rounded corners?
            if(rc > MoreMath.EPSILON) {
                // Restrict the corner radius according to the sector angle.
                if(da < Math.PI) {
                    val oc = if(da0 > MoreMath.EPSILON) intersect(x01, y01, x00, y00, x11, y11, x10, y10) else doubleArrayOf(x10, y10)
                    val ax = x01 - oc[0]
                    val ay = y01 - oc[1]
                    val bx = x11 - oc[0]
                    val by = y11 - oc[1]
                    val kc = 1 / Math.sin(Math.acos((ax * bx + ay * by) / (Math.sqrt(ax * ax + ay * ay) * Math.sqrt(bx * bx + by * by))) / 2)
                    val lc = Math.sqrt(oc[0] * oc[0] + oc[1] * oc[1])
                    rc0 = Math.min(rc, (r0 - lc) / (kc - 1))
                    rc1 = Math.min(rc, (r1 - lc) / (kc + 1))
                }
            }

            // Is the sector collapsed to a line?
            if(da1 <= MoreMath.EPSILON)
                context.moveTo(x01, y01)

            // Does the sector’s outer ring have rounded corners?
            else if(rc1 > MoreMath.EPSILON) {
                val t0 = cornerTangents(x00, y00, x01, y01, r1, rc1, if(cw) 1.0 else 0.0)
                val t1 = cornerTangents(x11, y11, x10, y10, r1, rc1, if(cw) 1.0 else 0.0)

                context.moveTo(t0.cx + t0.x01, t0.cy + t0.y01)

                // Have the corners merged?
                if(rc1 < rc)
                    context.arc(t0.cx, t0.cy, rc1, Math.atan2(t0.y01, t0.x01), Math.atan2(t1.y01, t1.x01), !cw)

                // Otherwise, draw the two corners and the ring.
                else {
                    context.arc(t0.cx, t0.cy, rc1, Math.atan2(t0.y01, t0.x01), Math.atan2(t0.y11, t0.x11), !cw)
                    context.arc(0.0, 0.0, r1, Math.atan2(t0.cy + t0.y11, t0.cx + t0.x11), Math.atan2(t1.cy + t1.y11, t1.cx + t1.x11), !cw)
                    context.arc(t1.cx, t1.cy, rc1, Math.atan2(t1.y11, t1.x11), Math.atan2(t1.y01, t1.x01), !cw)
                }
            }

            // Or is the outer ring just a circular arc?
            else {
                context.moveTo(x01, y01)
                context.arc(0.0, 0.0, r1, a01, a11, !cw)
            }

            // Is there no inner ring, and it’s a circular sector?
            // Or perhaps it’s an annular sector collapsed due to padding?
            if(r0 <= MoreMath.EPSILON || da0 <= MoreMath.EPSILON)
                context.lineTo(x10, y10)

            // Does the sector’s inner ring (or point) have rounded corners?
            else if(rc0 > MoreMath.EPSILON) {
                val t0 = cornerTangents(x10, y10, x11, y11, r0, -rc0, if(cw) 1.0 else 0.0)
                val t1 = cornerTangents(x01, y01, x00, y00, r0, -rc0, if(cw) 1.0 else 0.0)

                context.lineTo(t0.cx + t0.x01, t0.cy + t0.y01)

                // Have the corners merged?
                if(rc0 < rc)
                    context.arc(t0.cx, t0.cy, rc0, Math.atan2(t0.y01, t0.x01), Math.atan2(t1.y01, t1.x01), !cw)

                // Otherwise, draw the two corners and the ring.
                else {
                    context.arc(t0.cx, t0.cy, rc0, Math.atan2(t0.y01, t0.x01), Math.atan2(t0.y11, t0.x11), !cw);
                    context.arc(0.0, 0.0, r0, Math.atan2(t0.cy + t0.y11, t0.cx + t0.x11), Math.atan2(t1.cy + t1.y11, t1.cx + t1.x11), cw)
                    context.arc(t1.cx, t1.cy, rc0, Math.atan2(t1.y11, t1.x11), Math.atan2(t1.y01, t1.x01), !cw)
                }
            }

            // Or is the inner ring just a circular arc?
            else
                context.arc(0.0, 0.0, r0, a10, a00, cw)
        }

        context.closePath()

        return context
    }

    private fun asin(x: Double): Double  = when {
        x >= 1 -> MoreMath.HALF_PI
        x <= -1 -> -MoreMath.HALF_PI
        else -> Math.asin(x)
    }

    fun intersect(x0: Double, y0: Double, x1: Double, y1: Double, x2: Double, y2: Double, x3: Double, y3: Double): DoubleArray {
        val x10 = x1 - x0
        val y10 = y1 - y0
        val x32 = x3 - x2
        val y32 = y3 - y2
        val t = (x32 * (y0 - y2) - y32 * (x0 - x2)) / (y32 * x10 - x32 * y10)
        return doubleArrayOf(x0 + t * x10, y0 + t * y10)
    }

    // Compute perpendicular offset line of length rc.
    // http://mathworld.wolfram.com/Circle-LineIntersection.html
    private data class CornerTangents(val cx: Double, val cy: Double, val x01: Double, val y01: Double, val x11: Double, val y11: Double)
    private fun cornerTangents(x0: Double, y0: Double, x1: Double, y1: Double, r1: Double, rc: Double, cw: Double): CornerTangents {
        val x01 = x0 - x1
        val y01 = y0 - y1
        val lo = (if(cw.isTruthy()) rc else -rc) / Math.sqrt(x01 * x01 + y01 * y01)
        val ox = lo * y01
        val oy = -lo * x01
        val x11 = x0 + ox
        val y11 = y0 + oy
        val x10 = x1 + ox
        val y10 = y1 + oy
        val x00 = (x11 + x10) / 2.0
        val y00 = (y11 + y10) / 2.0
        val dx = x10 - x11
        val dy = y10 - y11
        val d2 = dx * dx + dy * dy
        val r = r1 - rc
        val D = x11 * y10 - x10 * y11
        val d = (if(dy < 0) -1 else 1) * Math.sqrt(Math.max(0.0, r * r * d2 - D * D))
        var cx0 = (D * dy - dx * d) / d2
        var cy0 = (-D * dx - dy * d) / d2
        val cx1 = (D * dy + dx * d) / d2
        val cy1 = (-D * dx + dy * d) / d2
        val dx0 = cx0 - x00
        val dy0 = cy0 - y00
        val dx1 = cx1 - x00
        val dy1 = cy1 - y00

        // Pick the closer of the two intersection points.
        // TODO Is there a faster way to determine which intersection to use?
        if(dx0 * dx0 + dy0 * dy0 > dx1 * dx1 + dy1 * dy1) {
            cx0 = cx1
            cy0 = cy1
        }

        return CornerTangents(cx0, cy0, -ox, -oy, cx0 * (r1 / r - 1), cy0 * (r1 / r - 1))
    }

    fun constant(d: Double): (T) -> Double = { d }
}
