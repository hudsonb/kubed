package kubed.shape.symbol

import javafx.scene.paint.Color
import kubed.path.Context
import kubed.path.PathContext
import kubed.shape.Shape
import kubed.util.MoreMath

class Symbol<T> : Shape<Symbol<T>, T>() {
    var type: (d: T) -> SymbolType = { symbolCircle() }
    var size: (d: T) -> Double = { 64.0 }

    fun type(value: SymbolType) = type { value }
    fun type(func: (T) -> SymbolType): Symbol<T> {
        type = func
        return this
    }

    fun size(value: Double) = size { value }
    fun size(func: (T) -> Double): Symbol<T> {
        size = func
        return this
    }

    override fun invoke(d: T): javafx.scene.shape.Shape {
        val symbol = type(d)
        val shape = symbol.create(size(d))
        apply(d, shape)

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
        val r = Math.sqrt(size / Math.PI)
        val circle = javafx.scene.shape.Circle()
        circle.radius = r
        circle.stroke = Color.BLACK
        return circle
    }
}

class Cross : PathSymbolType {
    override fun draw(context: Context, size: Double) {
        val r = Math.sqrt(size / 5) / 2
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
        private val tan30 = Math.sqrt(1.0 / 3.0)
        private val tan30_2 = tan30 * 2
    }

    override fun draw(context: Context, size: Double) {
        val y = Math.sqrt(size / tan30_2)
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
        val w = Math.sqrt(size)
        val x = -w / 2
        val rect = javafx.scene.shape.Rectangle(x, x, w, w)
        rect.stroke = Color.BLACK
        return rect
    }
}

class Star : PathSymbolType {
    companion object {
        private const val ka = 0.89081309152928522810
        private val kr = Math.sin(Math.PI / 10) / Math.sin(7 * Math.PI / 10)
        private val kx = Math.sin(MoreMath.TAU / 10) * kr
        private val ky = -Math.cos(MoreMath.TAU / 10) * kr
    }

    override fun draw(context: Context, size: Double) {
        val r = Math.sqrt(size * ka)
        val x = kx * r
        val y = ky * r
        context.moveTo(0.0, -r)
        context.lineTo(x, y)
        for(i in 1..4) {
            val a = MoreMath.TAU * i / 5
            val c = Math.cos(a)
            val s = Math.sin(a)
            context.lineTo(s * r, -c * r)
            context.lineTo(c * x - s * y, s * x + c * y)
        }
        context.closePath()
    }
}

class Triangle : PathSymbolType {
    companion object {
        private val sqrt3 = Math.sqrt(3.0)
    }

    override fun draw(context: Context, size: Double) {
        val y = -Math.sqrt(size / (sqrt3 * 3))
        context.moveTo(0.0, y * 2)
        context.lineTo(-sqrt3 * y, -y)
        context.lineTo(sqrt3 * y, -y)
        context.closePath()
    }
}

class Wye : PathSymbolType {
    companion object {
        private const val c = -0.5
        private val s = Math.sqrt(3.0) / 2
        private val k = 1 / Math.sqrt(12.0)
        private val a = (k / 2 + 1) * 3
    }

    override fun draw(context: Context, size: Double) {
        val r = Math.sqrt(size / a)
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