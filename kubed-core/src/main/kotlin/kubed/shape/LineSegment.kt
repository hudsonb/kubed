package kubed.shape

class LineSegment<T> : Shape<LineSegment<T>, T>() {
    var startX: (T) -> Double = { throw IllegalStateException("startX must be specified") }
    var startY: (T) -> Double = { throw IllegalStateException("startY must be specified") }

    var endX: (T) -> Double = { throw IllegalStateException("endX must be specified") }
    var endY: (T) -> Double = { throw IllegalStateException("endY must be specified") }

    fun startX(x: Double) = startX { x }
    fun startX(x: (T) -> Double): LineSegment<T> {
        startX = x
        return this
    }

    fun startY(y: Double) = startY { y }
    fun startY(y: (T) -> Double): LineSegment<T> {
        startY = y
        return this
    }

    fun endX(x: Double) = endX { x }
    fun endX(x: (T) -> Double): LineSegment<T> {
        endX = x
        return this
    }

    fun endY(y: Double) = endY { y }
    fun endY(y: (T) -> Double): LineSegment<T> {
        endY = y
        return this
    }

    override fun invoke(d: T): javafx.scene.shape.Shape {
        val line = javafx.scene.shape.Line()
        with(line) {
            line.startX = startX(d)
            line.startY = startY(d)
            line.endX = endX(d)
            line.endY = endY(d)
        }

        apply(d, line)

        return line
    }
}