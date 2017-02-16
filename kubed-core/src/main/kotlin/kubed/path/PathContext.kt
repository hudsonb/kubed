package kubed.path

import javafx.scene.Node
import javafx.scene.canvas.GraphicsContext
import javafx.scene.shape.*
import kubed.util.MoreMath
import kubed.util.isTruthy
import java.util.*

fun path(): PathContext = PathContext()

open class PathContext : Context {
    var x0: Double = Double.NaN
    var y0: Double = Double.NaN
    var x1: Double = Double.NaN
    var y1: Double = Double.NaN

    val elements = ArrayList<PathElement>()

    override fun moveTo(x: Double, y: Double): PathContext {
        x0 = x
        x1 = x
        y0 = y
        y1 = y

        elements += MoveTo(x, y)
        return this
    }

    override fun lineTo(x: Double, y: Double): PathContext {
        x0 = x
        x1 = x
        y0 = y
        y1 = y

        elements += LineTo(x, y)
        return this
    }

    override fun quadraticCurveTo(controlX: Double, controlY: Double, x: Double, y: Double): PathContext {
        x1 = x
        y1 = y

        elements += QuadCurveTo(controlX, controlY, x, y)
        return this
    }

    override fun bezierCurveTo(controlX: Double, controlY: Double, controlX2: Double, controlY2: Double, x: Double, y: Double): PathContext {
        x1 = x
        y1 = y

        elements += CubicCurveTo(controlX, controlY, controlX2, controlY2, x, y)
        return this
    }

    override fun arcTo(x1: Double, y1: Double, x2: Double, y2: Double, radius: Double): PathContext {
        // Is the radius negative? Error
        if(radius < 0)
            throw IllegalArgumentException("""negative radius $radius""")

        x0 = x1
        y0 = y1
        val x21 = x2 - x1
        val y21 = y2 - y1
        val x01 = x0 - x1
        val y01 = y0 - y1
        val l01_2 = x01 * x01 + y01 * y01

        // Is (x1,y1) coincident with (x0,y0)?
        if(l01_2 <= MoreMath.EPSILON)
            return this

        // Or, are (x0,y0), (x1,y1) and (x2,y2) collinear?
        // Equivalently, is (x1,y1) coincident with (x2,y2)?
        // Or, is the radius zero? Line to (x1,y1).
        if(Math.abs(y01 * x21 - y21 * x01) > MoreMath.EPSILON || radius == 0.0)
        {
            this.x1 = x1
            this.y1 = y1
            elements += LineTo(x1, y1)
        }
        else // Otherwise draw an arc
        {
            val x20 = x2 - x0
            val y20 = y2 - y0
            val l21_2 = x21 * x21 + y21 * y21
            val l20_2 = x20 * x20 + y20 * y20
            val l21 = Math.sqrt(l21_2)
            val l01 = Math.sqrt(l01_2)
            val l = radius * Math.tan((Math.PI - Math.acos((l21_2 + l01_2 - l20_2) / (2 * l21 * l01))) / 2.0)
            val t01 = l / l01
            val t21 = l / l21

            // If the start tangent is not coincident with (x0,y0), line to
            if(Math.abs(t01 - 1) > MoreMath.EPSILON)
            {
                elements += LineTo(x1 + t01 * x01, y1 + t01 * y01)
            }

            this.x1 = x1 + t21 * x21
            this.y1 = y1 + t21 * y21
            elements += ArcTo(radius, radius, 0.0, x1, y1, false, (y01 * x20 > x01 * y20))
        }

        return this
    }

    override fun arc(x: Double, y: Double, r: Double, a0: Double, a1: Double, ccw: Boolean): PathContext {
        // Is the radius negative? Error
        if(r < 0)
            throw IllegalArgumentException("""negative radius $r""")

        val dx = r * Math.cos(a0)
        val dy = r * Math.sin(a0)
        val x0 = x + dx
        val y0 = y + dy
        val cw = !ccw
        var da = if(ccw) a0 - a1 else a1 - a0

        // Is this path emptySelection? Move to (x0,y0).
        if(x1.isNaN()) {
            elements += MoveTo(x0, y0)
        }

        // Or, is (x0, y0) not coincident with the previous point? Line to (x0, y0).
        else if(Math.abs(x1 - x0) > MoreMath.EPSILON || Math.abs(y1 - y0) >MoreMath.EPSILON) {
            elements += LineTo(x0, y0)
        }

        // Is this arc emptySelection? We’re done.
        if(!r.isTruthy())
            return this

        // Is this a complete circle? Draw two arcs to complete the circle.
        if(da > MoreMath.TAU_EPSILON) {
            elements += ArcTo(r, r, 0.0, x - dx, y - dy, true, cw)

            x1 = x0
            y1 = y0
            elements += ArcTo(r, r, 0.0, x1, y1, true, cw)
        }

        // Otherwise, draw an arc!
        else {
            if(da < 0)
                da = da % MoreMath.TAU + MoreMath.TAU

            x1 = x + r * Math.cos(a1)
            y1 = y + r * Math.sin(a1)
            elements += ArcTo(r, r, 0.0, x1, y1, (da >= Math.PI), cw)
        }

        return this
    }

    override fun rect(x: Double, y: Double, w: Double, h: Double): PathContext {
        x0 = x
        x1 = x
        y0 = y
        y1 = y

        elements += MoveTo(x, y)
        elements += LineTo(x + w, y)
        elements += LineTo(x + w, y + h)
        elements += LineTo(x, y + h)
        elements += ClosePath()
        return this
    }

    override fun closePath(): PathContext {
        x1 = x0
        y1 = y1

        elements += ClosePath()
        return this
    }

    override operator fun invoke() = Path(elements)

    override operator fun invoke(gc: GraphicsContext) {
        gc.beginPath()
        for(e in elements) {
            when(e) {
                is MoveTo -> gc.moveTo(e.x, e.y)
                is LineTo -> gc.lineTo(e.x, e.y)
                is QuadCurveTo -> gc.quadraticCurveTo(e.controlX, e.controlY, e.x, e.y)
                is CubicCurveTo -> gc.bezierCurveTo(e.controlX1, e.controlY1, e.controlX2, e.controlY2, e.x, e.y)
                //is ArcTo -> throw UnsupportedOperationException("ArcTo is not yet supported")
            }
        }
    }
}
