package kubed.shape

class Rectangle<T> : Shape<Rectangle<T>, T>() {
    var width: (T, Int) -> Double = { _, _ -> throw IllegalStateException("width must be specified") }
    var height: (T, Int) -> Double = { _, _ -> throw IllegalStateException("height must be specified") }
    var arcWidth: (T, Int) -> Double = { _, _ -> 0.0 }
    var arcHeight: (T, Int) -> Double = { _, _ -> 0.0 }

    fun width(w: Double) = width { _, _ -> w }
    fun width(w: (T, Int) -> Double): Rectangle<T> {
        width = w
        return this
    }

    fun height(h: Double) = height { _, _ -> h }
    fun height(h: (T, Int) -> Double): Rectangle<T> {
        height = h
        return this
    }

    fun arcWidth(w: Double) = arcWidth { _, _ -> w }
    fun arcWidth(w: (T, Int) -> Double): Rectangle<T> {
        arcWidth = w
        return this
    }

    fun arcHeight(h: Double) = arcHeight { _, _ -> h }
    fun arcHeight(h: (T, Int) -> Double): Rectangle<T> {
        height = h
        return this
    }

    override operator fun invoke(d: T, i: Int): javafx.scene.shape.Shape {
        val rect = javafx.scene.shape.Rectangle()
        with(rect) {
            width = width(d, i)
            height = height(d, i)
            arcWidth = arcWidth(d, i)
            arcHeight = arcHeight(d, i)
        }

        apply(d, i, rect)

        return rect
    }
}