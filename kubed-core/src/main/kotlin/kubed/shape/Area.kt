package kubed.shape

import kubed.path.Context
import kubed.path.PathContext
import kubed.shape.curve.Curve
import kubed.shape.curve.LinearCurve

class Area<T> : PathShape<Area<T>, List<T>>() {
    var x: (T, Int, List<T>) -> Double
        get() = x0
        set(value) {
            x0 = value
            x1 = null
        }
    var x0: (T, Int, List<T>) -> Double = { _, _, _ -> throw IllegalStateException("x0 must be specified") }
    var x1: ((T, Int, List<T>) -> Double)? = null
    var y: (T, Int, List<T>) -> Double
        get() = y0
        set(value) {
            y0 = value
            y1 = null
        }
    var y0: (T, Int, List<T>) -> Double = constant(0.0)
    var y1: ((T, Int, List<T>) -> Double)? = null
    var defined: (T, Int, List<T>) -> Boolean = { _, _, _ -> true }
    var curve: (Context) -> Curve = ::LinearCurve

    fun x(func: (T, Int, List<T>) -> Double): Area<T> {
        x = func
        return this
    }

    fun x(value: Double): Area<T> {
        x = constant(value)
        return this
    }

    fun x0(func: (T, Int, List<T>) -> Double): Area<T> {
        x0 = func
        return this
    }

    fun x0(value: Double): Area<T> {
        x0 = constant(value)
        return this
    }

    fun x1(func: (T, Int, List<T>) -> Double): Area<T> {
        x1 = func
        return this
    }

    fun x1(value: Double): Area<T> {
        x1 = constant(value)
        return this
    }

    fun y(func: (T, Int, List<T>) -> Double): Area<T> {
        y = func
        return this
    }

    fun y(value: Double): Area<T> {
        y = constant(value)
        return this
    }

    fun y0(func: (T, Int, List<T>) -> Double): Area<T> {
        y0 = func
        return this
    }

    fun y0(value: Double): Area<T> {
        y0 = constant(value)
        return this
    }

    fun y1(func: (T, Int, List<T>) -> Double): Area<T> {
        y1 = func
        return this
    }

    fun y1(value: Double): Area<T> {
        y1 = constant(value)
        return this
    }

    fun defined(func: (T, Int, List<T>) -> Boolean): Area<T> {
        defined = func
        return this
    }

    fun curve(func: (Context) -> Curve): Area<T> {
        curve = func
        return this
    }

    override fun generate(data: List<T>): Context {
        val context = PathContext()
        val output = curve(context)

        var defined0 = false

        val n = data.size
        var j = 0
        val x0z = DoubleArray(n)
        val y0z = DoubleArray(n)
        for(i in 0..n) {
            if(!(i < n && defined(data[i], i, data)) == defined0) {
                defined0 = !defined0
                if(defined0) {
                    j = i
                    output.areaStart()
                    output.lineStart()
                }
                else {
                    output.lineEnd()
                    output.lineStart()
                    for(k in i - 1 downTo j) {
                        output.point(x0z[k], y0z[k])
                    }
                    output.lineEnd()
                    output.areaEnd()
                }
            }
            if(defined0) {
                val datum = data[i]
                x0z[i] = x0(datum, i, data)
                y0z[i] = y0(datum, i, data)

                output.point(x1?.invoke(datum, i, data) ?: x0z[i], y1?.invoke(datum, i, data) ?: y0z[i])
            }
        }

        return context
    }

    fun constant(value: Double) = { _: T, _: Int, _: List<T> -> value }
}
