package kubed.shape

import kubed.path.Context
import kubed.shape.curve.Curve
import kubed.shape.curve.curveLinear
import kubed.shape.curve.curveRadial

class RadialArea<T> : PathShape<RadialArea<T>, List<T>>() {
    private val area = Area<T>()

    init {
        area.x = { _, _, _ -> throw IllegalStateException("angle must be specified") }
        area.x0 = { _, _, _ -> throw IllegalStateException("startAngle must be specified") }
        area.x1 = null
        area.y = { _, _, _ -> throw IllegalStateException("radius must be specified") }
        area.y0 = { _, _, _ -> throw IllegalStateException("innerRadius must be specified") }
        area.y1 = null
        area.curve = curveRadial(curveLinear())
    }

    var angle: (T, Int, List<T>) -> Double
        get() = area.x
        set(value) { area.x = value }

    var startAngle: (T, Int, List<T>) -> Double
        get() = area.x0
        set(value) { area.x0 = value }

    var endAngle: ((T, Int, List<T>) -> Double)?
        get() = area.x1
        set(value) { area.x1 = value }

    var innerRadius: (T, Int, List<T>) -> Double
        get() = area.y0
        set(value) { area.y0 = value }

    var outerRadius: ((T, Int, List<T>) -> Double)?
        get() = area.y1
        set(value) { area.y1 = value }

    var radius: (T, Int, List<T>) -> Double
        get() = area.y
        set(value) { area.y = value }

    var defined: (T, Int, List<T>) -> Boolean
        get() = area.defined
        set(value) { area.defined = value }

    var curve: (Context) -> Curve
        get() = area.curve
        set(value) { area.curve = curveRadial(value) }

    fun angle(value: Double) = angle { _, _, _ -> value}
    fun angle(func: (T, Int, List<T>) -> Double): RadialArea<T> {
        angle = func
        return this
    }

    fun radius(value: Double) = radius { _, _, _ -> value }
    fun radius(func: (T, Int, List<T>) -> Double): RadialArea<T> {
        radius = func
        return this
    }

    fun curve(func: (Context) -> Curve): RadialArea<T> {
        curve = func
        return this
    }

    override fun generate(d: List<T>, i: Int) = area.generate(d, i)
}
