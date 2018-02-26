package kubed.shape

import javafx.geometry.Point2D
import kubed.path.Context
import kubed.path.PathContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val thirdPi = PI / 3
private val angles = listOf(0.0, thirdPi, 2 * thirdPi, 3 * thirdPi, 4 * thirdPi, 5 * thirdPi)
//private val flatAngles = listOf(0.5 * thirdPi, 1.5 * thirdPi, 2.5 * thirdPi, 3.5 * thirdPi, 4.5 * thirdPi, 5.5 * thirdPi)

fun <T> hexagon(init: Hexagon<T>.() -> Unit) = Hexagon<T>().apply(init)

class Hexagon<T> : PathShape<Hexagon<T>, T>() {
    var radius: (T, Int) -> Double = { _, _ -> throw IllegalStateException("radius must be specified") }
    var centerX: (T, Int) -> Double = { _, _ -> 0.0 }
    var centerY: (T, Int) -> Double = { _, _ -> 0.0 }

    fun radius(r: Double) = radius { _, _ -> r }
    fun radius(r: (T, Int) -> Double): Hexagon<T> {
        radius = r
        return this
    }

    fun centerX(cx: Double) = centerX { _, _ -> cx }
    fun centerX(cx: (T, Int) -> Double): Hexagon<T> {
        centerX = cx
        return this
    }

    fun centerY(cy: Double) = centerY { _, _ -> cy }
    fun centerY(cy: (T, Int) -> Double): Hexagon<T> {
        centerY = cy
        return this
    }

    override fun generate(d: T, i: Int): Context? {
        val context = PathContext()

        val r = radius(d, i)
        var x = centerX(d, i)
        var y = centerY(d, i)
        var first = true
        for(p in hexagon(r)) {
            x += p.x
            y += p.y
            if(first) {
                first = false
                context.moveTo(x, y)
            }
            else context.lineTo(x, y)
        }
        context.closePath()

        return context
    }

    private fun hexagon(r: Double): List<Point2D> {
        var x0 = 0.0
        var y0 = 0.0
        return angles.map {
            val x1 = sin(it) * r
            val y1 = -cos(it) * r
            val dx = x1 - x0
            val dy = y1 - y0
            x0 = x1
            y0 = y1
            Point2D(dx, dy)
        }
    }
}
