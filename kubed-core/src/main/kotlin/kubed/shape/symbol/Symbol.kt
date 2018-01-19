package kubed.shape.symbol

import javafx.geometry.Point2D
import javafx.scene.paint.Color
import kubed.math.TAU
import kubed.path.Context
import kubed.path.PathContext
import kubed.shape.Shape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Symbol<T> : Shape<Symbol<T>, T>() {
    var type: (T, Int) -> SymbolType = { _, _ -> symbolCircle() }
    var size: (T, Int) -> Double = { _, _ -> 64.0 }

    fun type(value: SymbolType) = type { _, _ -> value }
    fun type(func: (T, Int) -> SymbolType): Symbol<T> {
        type = func
        return this
    }

    fun size(value: Double) = size { _, _ -> value }
    fun size(func: (T, Int) -> Double): Symbol<T> {
        size = func
        return this
    }

    override fun invoke(d: T, i: Int): javafx.scene.shape.Shape {
        val symbol = type(d, i)
        val shape = symbol.create(size(d, i))
        apply(d, i, shape)

        return shape
    }
}

interface SymbolType {
    fun create(size: Double): javafx.scene.shape.Shape
}

internal interface PathSymbolType : SymbolType {
    fun draw(context: Context, size: Double)

    override fun create(size: Double): javafx.scene.shape.Shape {
        val context = PathContext()
        draw(context, size)
        return context()
    }
}

class Circle : SymbolType {
    override fun create(size: Double): javafx.scene.shape.Shape {
        val r = sqrt(size / PI)
        val circle = javafx.scene.shape.Circle()
        circle.radius = r
        circle.stroke = Color.BLACK
        return circle
    }
}

class Cross : PathSymbolType {
    override fun draw(context: Context, size: Double) {
        val r = sqrt(size / 5) / 2
        context.moveTo(-3 * r, -r)
        context.lineTo(-r, -r)
        context.lineTo(-r, -3 * r)
        context.lineTo(r, -3 * r)
        context.lineTo(r, -r)
        context.lineTo(3 * r, -r)
        context.lineTo(3 * r, r)
        context.lineTo(r, r)
        context.lineTo(r, 3 * r)
        context.lineTo(-r, 3 * r)
        context.lineTo(-r, r)
        context.lineTo(-3 * r, r)
        context.closePath()
    }
}

class Diamond : PathSymbolType {
    companion object {
        private val tan30 = sqrt(1.0 / 3.0)
        private val tan30_2 = tan30 * 2
    }

    override fun draw(context: Context, size: Double) {
        val y = sqrt(size / tan30_2)
        val x = y * tan30
        context.moveTo(0.0, -y)
        context.lineTo(x, 0.0)
        context.lineTo(0.0, y)
        context.lineTo(-x, 0.0)
        context.closePath()
    }
}

class Square : SymbolType {
    override fun create(size: Double): javafx.scene.shape.Shape {
        val w = sqrt(size)
        val x = -w / 2
        val rect = javafx.scene.shape.Rectangle(x, x, w, w)
        rect.stroke = Color.BLACK
        return rect
    }
}

class Star : PathSymbolType {
    companion object {
        private const val ka = 0.89081309152928522810
        private val kr = sin(PI / 10) / sin(7 * PI / 10)
        private val kx = sin(TAU / 10) * kr
        private val ky = -cos(TAU / 10) * kr
    }

    override fun draw(context: Context, size: Double) {
        val r = sqrt(size * ka)
        val x = kx * r
        val y = ky * r
        context.moveTo(0.0, -r)
        context.lineTo(x, y)
        for(i in 1..4) {
            val a = TAU * i / 5
            val c = cos(a)
            val s = sin(a)
            context.lineTo(s * r, -c * r)
            context.lineTo(c * x - s * y, s * x + c * y)
        }
        context.closePath()
    }
}

class Triangle : PathSymbolType {
    companion object {
        private val sqrt3 = sqrt(3.0)
    }

    override fun draw(context: Context, size: Double) {
        val y = -sqrt(size / (sqrt3 * 3))
        context.moveTo(0.0, y * 2)
        context.lineTo(-sqrt3 * y, -y)
        context.lineTo(sqrt3 * y, -y)
        context.closePath()
    }
}

class Wye : PathSymbolType {
    companion object {
        private const val c = -0.5
        private val s = sqrt(3.0) / 2
        private val k = 1 / sqrt(12.0)
        private val a = (k / 2 + 1) * 3
    }

    override fun draw(context: Context, size: Double) {
        val r = sqrt(size / a)
        val x0 = r / 2
        val y0 = r * k
        val x1 = x0
        val y1 = r * k + r
        val x2 = -x1
        val y2 = y1
        context.moveTo(x0, y0)
        context.lineTo(x1, y1)
        context.lineTo(x2, y2)
        context.lineTo(c * x0 - s * y0, s * x0 + c * y0)
        context.lineTo(c * x1 - s * y1, s * x1 + c * y1)
        context.lineTo(c * x2 - s * y2, s * x2 + c * y2)
        context.lineTo(c * x0 + s * y0, c * y0 - s * x0)
        context.lineTo(c * x1 + s * y1, c * y1 - s * x1)
        context.lineTo(c * x2 + s * y2, c * y2 - s * x2)
        context.closePath()
    }
}

class Hexagon(val theta: Double = TAU / 12) : PathSymbolType {
    private fun sideLength(area: Double) = sqrt((2 * area) / (3 * sqrt(3.0)))

    override fun draw(context: Context, size: Double) {
        val s = sideLength(size)

        rotatePoint(s, 0.0, theta).apply { context.moveTo(x, y) }

        for(i in 0..5) {
            val a = TAU * i / 6
            val x = cos(a) * s
            val y = sin(a) * s

            val p = rotatePoint(x, y, theta)
            context.lineTo(p.x, p.y)
        }

        context.closePath()
    }
}

class Pentagon : PathSymbolType {
    companion object {
        private val circumradiusCoeff = 1.0 / 10.0 * sqrt(50 + 10 * sqrt(5.0))
    }

    private fun circumradius(sideLength: Double) = sideLength * circumradiusCoeff

    private fun sideLength(area: Double) = sqrt((4 * area) / sqrt(5 * (5 + 2 * sqrt(5.0))))

    override fun draw(context: Context, size: Double) {
        val s = sideLength(size)
        val r = circumradius(s)
        val theta = -TAU / 4

        var p = rotatePoint(r, 0.0, theta)
        context.moveTo(p.x, p.y)

        for (i in 0..4) {
            val a = TAU * i / 5
            val x = cos(a) * r
            val y = sin(a) * r

            p = rotatePoint(x, y, theta)
            context.lineTo(p.x, p.y)
        }

        context.closePath()
    }
}

class X : PathSymbolType {
    override fun draw(context: Context, size: Double) {
        val r = sqrt(size / 5) / 2
        val theta = TAU / 8

        with(context) {
            rotatePoint(-3 * r, -r, theta).apply { moveTo(x, y) }
            rotatePoint(-r, -r, theta).apply { lineTo(x, y) }
            rotatePoint(-r, -3 * r, theta).apply { lineTo(x, y) }
            rotatePoint(r, -3 * r, theta).apply { lineTo(x, y) }
            rotatePoint(r, -r, theta).apply { lineTo(x, y) }
            rotatePoint(3 * r, -r, theta).apply { lineTo(x, y) }
            rotatePoint(3 * r, r, theta).apply { lineTo(x, y) }
            rotatePoint(r, r, theta).apply { lineTo(x, y) }
            rotatePoint(r, 3 * r, theta).apply { lineTo(x, y) }
            rotatePoint(-r, 3 * r, theta).apply { lineTo(x, y) }
            rotatePoint(-r, r, theta).apply { lineTo(x, y) }
            rotatePoint(-3 * r, r, theta).apply { lineTo(x, y) }
        }

        context.closePath()
    }
}

private fun rotatePoint(x: Double, y: Double, theta: Double) = Point2D(cos(theta) * x + -sin(theta) * y,
                                                                       sin(theta) * x + cos(theta) * y)

