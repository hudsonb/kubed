package kubed.shape

import kubed.path.Context
import kubed.shape.curve.Curve
import kubed.shape.curve.curveLinear
import kubed.shape.curve.curveRadial

class RadialLine<T> : PathShape<RadialLine<T>, List<T>>() {
    private val line = Line<T>()

    init {
        line.x = { _, _, _ -> throw IllegalStateException("angle must be specified") }
        line.y = { _, _, _ -> throw IllegalStateException("radius must be specified") }
        line.curve = curveRadial(curveLinear())
    }

    var angle: (T, Int, List<T>) -> Double
        get() = line.x
        set(value) {
            line.x = value
        }

    var radius: (T, Int, List<T>) -> Double
        get() = line.y
        set(value) {
            line.y = value
        }

    var defined: (T, Int, List<T>) -> Boolean
        get() = line.defined
        set(value) {
            line.defined = value
        }

    var curve: (Context) -> Curve
        get() = line.curve
        set(value) {
            line.curve = curveRadial(value)
        }

    fun angle(value: Double) = angle { _, _, _ -> value }
    fun angle(func: (T, Int, List<T>) -> Double): RadialLine<T> {
        angle = func
        return this
    }

    fun radius(value: Double) = radius { _, _, _ -> value }
    fun radius(func: (T, Int, List<T>) -> Double): RadialLine<T> {
        radius = func
        return this
    }

    fun curve(func: (Context) -> Curve): RadialLine<T> {
        curve = func
        return this
    }

    override fun generate(d: List<T>) = line.generate(d)
}