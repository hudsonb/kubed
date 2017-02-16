package kubed.shape

class Circle<T> : Shape<Circle<T>, T>() {
    var radius: (T) -> Double = { throw IllegalStateException("radius must be specified") }

    fun radius(r: Double) = radius { r }
    fun radius(r: (T) -> Double): Circle<T> {
        radius = r
        return this
    }

    override operator fun invoke(d: T): javafx.scene.shape.Shape {
        val circle = javafx.scene.shape.Circle()
        circle.radius = radius(d)
        apply(d, circle)
        return circle
    }
}