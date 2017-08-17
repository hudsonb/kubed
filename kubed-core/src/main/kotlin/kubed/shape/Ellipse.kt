package kubed.shape

class Ellipse<T> : Shape<Ellipse<T>, T>() {
    var radiusX: (T, Int) -> Double = { _, _ -> throw IllegalStateException("radiusX must be specified") }
    var radiusY: (T, Int) -> Double = { _, _ -> throw IllegalStateException("radiusY must be specified") }

    fun radiusX(r: Double) = radiusX { _, _ -> r }
    fun radiusX(r: (T, Int) -> Double): Ellipse<T> {
        radiusX = r
        return this
    }

    fun radiusY(r: Double) = radiusY { _, _ -> r }
    fun radiusY(r: (T, Int) -> Double): Ellipse<T> {
        radiusY = r
        return this
    }

    override operator fun invoke(d: T, i: Int): javafx.scene.shape.Shape {
        val ellipse = javafx.scene.shape.Ellipse(radiusX(d, i), radiusY(d, i))
        apply(d, i, ellipse)
        return ellipse
    }
}