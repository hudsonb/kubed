package kubed.shape

class Rectangle<T> : Shape<Rectangle<T>, T>() {
    var width: (T) -> Double = { throw IllegalStateException("width must be specified") }
    var height: (T) -> Double = { throw IllegalStateException("height must be specified") }
    var arcWidth: (T) -> Double = { 0.0 }
    var arcHeight: (T) -> Double = { 0.0 }

    fun width(w: Double) = width { w }
    fun width(w: (T) -> Double): Rectangle<T> {
        width = w
        return this
    }

    fun height(h: Double) = height { h }
    fun height(h: (T) -> Double): Rectangle<T> {
        height = h
        return this
    }

    fun arcWidth(w: Double) = arcWidth { w }
    fun arcWidth(w: (T) -> Double): Rectangle<T> {
        arcWidth = w
        return this
    }

    fun arcHeight(h: Double) = arcHeight { h }
    fun arcHeight(h: (T) -> Double): Rectangle<T> {
        height = h
        return this
    }

    override operator fun invoke(d: T): javafx.scene.shape.Shape {
        val rect = javafx.scene.shape.Rectangle()
        with(rect) {
            width = width(d)
            height = height(d)
            arcWidth = arcWidth(d)
            arcHeight = arcHeight(d)
        }

        apply(d, rect)

        return rect
    }
}