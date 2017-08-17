package kubed.shape

class Polygon<T> : Shape<Polygon<T>, List<T>>() {
    var x: (T, Int, List<T>) -> Double = { _, _, _ -> throw IllegalStateException("x must be specified") }
    var y: (T, Int, List<T>) -> Double = { _, _, _ -> throw IllegalStateException("y must be specified") }

    fun x(value: Double) = x { _, _, _ -> value }
    fun x(func: (T, Int, List<T>) -> Double): Polygon<T> {
        x = func
        return this
    }

    fun y(value: Double) = y { _, _, _ -> value }
    fun y(func: (T, Int, List<T>) -> Double): Polygon<T> {
        y = func
        return this
    }

    override fun invoke(d: List<T>, i: Int): javafx.scene.shape.Shape {
        val poly = javafx.scene.shape.Polygon(*DoubleArray(d.size * 2, {
            if (it % 2 != 0)
                x(d[it], it, d)
            else
                y(d[it], it, d)
        }))

        apply(d, i, poly)

        return poly
    }
}