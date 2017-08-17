package kubed.shape

class LineSegment<T> : Shape<LineSegment<T>, T>() {
    var startX: (T, Int) -> Double = { _, _ -> throw IllegalStateException("startX must be specified") }
    var startY: (T, Int) -> Double = { _, _ -> throw IllegalStateException("startY must be specified") }

    var endX: (T, Int) -> Double = { _, _ -> throw IllegalStateException("endX must be specified") }
    var endY: (T, Int) -> Double = { _, _ -> throw IllegalStateException("endY must be specified") }

    fun startX(x: Double) = startX { _, _ -> x }
    fun startX(x: (T, Int) -> Double): LineSegment<T> {
        startX = x
        return this
    }

    fun startY(y: Double) = startY { _, _ -> y }
    fun startY(y: (T, Int) -> Double): LineSegment<T> {
        startY = y
        return this
    }

    fun endX(x: Double) = endX { _, _ -> x }
    fun endX(x: (T, Int) -> Double): LineSegment<T> {
        endX = x
        return this
    }

    fun endY(y: Double) = endY { _, _ -> y }
    fun endY(y: (T, Int) -> Double): LineSegment<T> {
        endY = y
        return this
    }

    override fun invoke(d: T, i: Int): javafx.scene.shape.Shape {
        val line = javafx.scene.shape.Line()
        with(line) {
            line.startX = startX(d, i)
            line.startY = startY(d, i)
            line.endX = endX(d, i)
            line.endY = endY(d, i)
        }

        apply(d, i, line)

        return line
    }
}