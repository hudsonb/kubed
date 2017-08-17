package kubed.shape

class Polyline<T> : Shape<Polyline<T>, List<T>>() {
    var x: (T, Int, List<T>) -> Double = { _, _, _ -> throw IllegalStateException("x must be specified") }
    var y: (T, Int, List<T>) -> Double = { _, _, _ -> throw IllegalStateException("y must be specified") }

    fun x(value: Double) = x { _, _, _ -> value }
    fun x(func: (T, Int, List<T>) -> Double): Polyline<T> {
        x = func
        return this
    }

    fun y(value: Double) = y { _, _, _ -> value }
    fun y(func: (T, Int, List<T>) -> Double): Polyline<T> {
        y = func
        return this
    }

    override fun invoke(d: List<T>, i: Int): javafx.scene.shape.Shape {
        val line = javafx.scene.shape.Polyline(*DoubleArray(d.size * 2, {
            if(it % 2 != 0)
                x(d[it], it, d)
            else
                y(d[it], it, d)
        }))

        apply(d, i, line)

        return line
    }
}