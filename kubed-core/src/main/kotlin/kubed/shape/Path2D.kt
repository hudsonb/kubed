package kubed.shape
import javafx.scene.shape.*
import javafx.scene.transform.Transform
import kubed.math.toDegrees
import java.awt.geom.AffineTransform
import java.awt.geom.Arc2D
import java.awt.geom.Arc2D.OPEN
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import kotlin.math.abs
import kotlin.math.sqrt

fun Transform.toAffineTransform(): AffineTransform {
    if(!isType2D) throw UnsupportedOperationException("Conversion of 3D transforms is unsupported")
    return AffineTransform(mxx, myx, mxy, myy, tx, ty)
}

val Path.totalLength: Double
    get() {
        var length = 0.0

        val coords = DoubleArray(6)
        var pt = 0 // Previous segment type
        var px = 0.0 // Previous x-coordinate
        var py = 0.0 // Previous y-coordinate
        var mx = 0.0 // Last move to x-coordinate
        var my = 0.0 // Last move to y-coordinate
        val pit = toPath2D().getPathIterator(localToParentTransform.toAffineTransform(), 1.0)
        while(!pit.isDone) {
            val type = pit.currentSegment(coords)

            val x = coords[0]
            val y = coords[1]

            when(type) {
                PathIterator.SEG_MOVETO -> {
                    mx = x
                    my = y
                }

                PathIterator.SEG_LINETO -> {
                    val dx = x - px
                    val dy = y - py
                    val l = sqrt(dx * dx + dy * dy)
                    if(l >= 1 || pt == PathIterator.SEG_MOVETO) length += l
                }

                PathIterator.SEG_CLOSE -> {
                    val dx = x - mx
                    val dy = y - my
                    val l = sqrt(dx * dx + dy * dy)
                    if(l >= 1 || pt == PathIterator.SEG_MOVETO) length += l
                }
            }

            pt = type
            px = x
            py = y
            pit.next()
        }

        return length
    }

fun Path.toPath2D(): Path2D {
    val path: Path2D = Path2D.Double(if(fillRule == FillRule.EVEN_ODD) Path2D.WIND_EVEN_ODD else Path2D.WIND_NON_ZERO)

    for(e in elements) {
        when(e) {
            is Arc2D -> append(e as ArcTo, path) // Why isn't this smart casted?
            is ClosePath -> path.closePath()
            is CubicCurveTo -> append(e, path)
            is HLineTo -> append(e, path)
            is LineTo -> append(e, path)
            is MoveTo -> append(e, path)
            is QuadCurveTo -> append(e, path)
            is VLineTo -> append(e, path)
            else -> throw UnsupportedOperationException("Path contains unknown PathElement type: " + e::class.qualifiedName)
        }
    }

    return path
}

private fun append(arcTo: ArcTo, path: Path2D) {
    val x0 = path.currentPoint.x
    val y0 = path.currentPoint.y

    val localX = arcTo.x
    val localY = arcTo.y
    val localSweepFlag = arcTo.isSweepFlag
    val localLargeArcFlag = arcTo.isLargeArcFlag

    // Determine target "to" position
    val xto = if(arcTo.isAbsolute) localX else localX + x0
    val yto = if(arcTo.isAbsolute) localY else localY + y0
    // Compute the half distance between the current and the final point
    val dx2 = (x0 - xto) / 2.0
    val dy2 = (y0 - yto) / 2.0
    // Convert angle from degrees to radians
    val xAxisRotationR = Math.toRadians(arcTo.xAxisRotation)
    val cosAngle = Math.cos(xAxisRotationR)
    val sinAngle = Math.sin(xAxisRotationR)

    //
    // Step 1 : Compute (x1, y1)
    //
    val x1 = cosAngle * dx2 + sinAngle * dy2
    val y1 = -sinAngle * dx2 + cosAngle * dy2
    // Ensure radii are large enough
    var rx = abs(arcTo.radiusX)
    var ry = abs(arcTo.radiusY)
    var Prx = rx * rx
    var Pry = ry * ry
    val Px1 = x1 * x1
    val Py1 = y1 * y1
    // check that radii are large enough
    val radiiCheck = Px1 / Prx + Py1 / Pry
    if (radiiCheck > 1.0) {
        rx *= sqrt(radiiCheck)
        ry *= sqrt(radiiCheck)
        if(rx == rx && ry == ry) {/* not NANs */ }
        else {
            path.lineTo(xto, yto)
            return
        }
        Prx = rx * rx
        Pry = ry * ry
    }

    //
    // Step 2 : Compute (cx1, cy1)
    //
    var sign = if (localLargeArcFlag == localSweepFlag) -1.0 else 1.0
    var sq = (Prx * Pry - Prx * Py1 - Pry * Px1) / (Prx * Py1 + Pry * Px1)
    sq = if (sq < 0.0) 0.0 else sq
    val coef = sign * Math.sqrt(sq)
    val cx1 = coef * (rx * y1 / ry)
    val cy1 = coef * -(ry * x1 / rx)

    //
    // Step 3 : Compute (cx, cy) from (cx1, cy1)
    //
    val sx2 = (x0 + xto) / 2.0
    val sy2 = (y0 + yto) / 2.0
    val cx = sx2 + (cosAngle * cx1 - sinAngle * cy1)
    val cy = sy2 + (sinAngle * cx1 + cosAngle * cy1)

    //
    // Step 4 : Compute the angleStart (angle1) and the angleExtent (dangle)
    //
    val ux = (x1 - cx1) / rx
    val uy = (y1 - cy1) / ry
    val vx = (-x1 - cx1) / rx
    val vy = (-y1 - cy1) / ry
    // Compute the angle start
    var n = sqrt(ux * ux + uy * uy)
    var p = ux // (1 * ux) + (0 * uy)
    sign = if (uy < 0.0) -1.0 else 1.0
    var angleStart = (sign * Math.acos(p / n)).toDegrees()

    // Compute the angle extent
    n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy))
    p = ux * vx + uy * vy
    sign = if (ux * vy - uy * vx < 0.0) -1.0 else 1.0
    var angleExtent = Math.toDegrees(sign * Math.acos(p / n))
    if(!localSweepFlag && angleExtent > 0) angleExtent -= 360.0
    else if(localSweepFlag && angleExtent < 0) angleExtent += 360.0

    angleExtent %= 360
    angleStart %= 360

    //
    // We can now build the resulting Arc2D
    //
    val arcX = cx - rx
    val arcY = cy - ry
    val arcW = rx * 2.0
    val arcH = ry * 2.0
    val arcStart = -angleStart
    val arcExtent = -angleExtent

    val arc = Arc2D.Double(OPEN).apply { setArc(arcX, arcY, arcW, arcH, arcStart, arcExtent, OPEN) }
    val xform: AffineTransform? = when(xAxisRotationR) {
        0.0 -> null
        else -> AffineTransform().apply { setToRotation(xAxisRotationR, cx, cy) }
    }

    val pi = arc.getPathIterator(xform)
    // RT-8926, append(true) converts the initial moveTo into a
    // lineTo which can generate huge miter joins if the segment
    // is small enough.  So, we manually skip it here instead.
    pi.next()
    path.append(pi, true)
}

private fun append(cubicCurveTo: CubicCurveTo, path: Path2D) {
    if(cubicCurveTo.isAbsolute) {
        path.curveTo(cubicCurveTo.controlX1, cubicCurveTo.controlY1,
                     cubicCurveTo.controlX2, cubicCurveTo.controlY2,
                     cubicCurveTo.x, cubicCurveTo.y)
    }
    else {
        val dx = path.currentPoint.x
        val dy = path.currentPoint.y
        path.curveTo(cubicCurveTo.controlX1 + dx, cubicCurveTo.controlY1 + dy,
                cubicCurveTo.controlX2 + dx, cubicCurveTo.controlY2 + dy,
                cubicCurveTo.x + dx, cubicCurveTo.y + dy)
    }
}

private fun append(hLineTo: HLineTo, path: Path2D) {
    if(hLineTo.isAbsolute) path.lineTo(hLineTo.x, path.currentPoint.y)
    else path.lineTo(path.currentPoint.x + hLineTo.x, path.currentPoint.y)
}

private fun append(lineTo: LineTo, path: Path2D) {
    if(lineTo.isAbsolute) path.lineTo(lineTo.x, lineTo.y)
    else path.lineTo(path.currentPoint.x + lineTo.x, path.currentPoint.y + lineTo.y)
}

private fun append(moveTo: MoveTo, path: Path2D) {
    if(moveTo.isAbsolute) path.moveTo(moveTo.x, moveTo.y)
    else path.moveTo((path.currentPoint.x + moveTo.x), path.currentPoint.y + moveTo.y)
}

private fun append(quadCurveTo: QuadCurveTo, path: Path2D) {
    if(quadCurveTo.isAbsolute) {
        path.quadTo(quadCurveTo.controlX, quadCurveTo.controlY,
                    quadCurveTo.x, quadCurveTo.y)
    }
    else {
        val dx = path.currentPoint.x
        val dy = path.currentPoint.y
        path.quadTo(quadCurveTo.controlX + dx, quadCurveTo.controlY + dy,
                quadCurveTo.x + dx, quadCurveTo.y + dy)
    }
}

private fun append(vLineTo: VLineTo, path: Path2D) {
    if(vLineTo.isAbsolute) path.lineTo(path.currentPoint.x, vLineTo.y)
    else path.lineTo(path.currentPoint.x, path.currentPoint.y + vLineTo.y)
}



