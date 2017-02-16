package kubed.shape

class Ellipse<T> : Shape<Ellipse<T>, T>() {
    var radiusX: (T) -> Double = { throw IllegalStateException("radiusX must be specified") }
    var radiusY: (T) -> Double = { throw IllegalStateException("radiusY must be specified") }

    fun radiusX(r: Double) = radiusX { r }
    fun radiusX(r: (T) -> Double): Ellipse<T> {
        radiusX = r
        return this
    }

    fun radiusY(r: Double) = radiusY { r }
    fun radiusY(r: (T) -> Double): Ellipse<T> {
        radiusY = r
        return this
    }

    override operator fun invoke(d: T): javafx.scene.shape.Shape {
        val ellipse = javafx.scene.shape.Ellipse(radiusX(d), radiusY(d))
        apply(d, ellipse)
        return ellipse
    }
}