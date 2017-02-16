package kubed.shape

class QuadCurve<T> : Shape<QuadCurve<T>, T>() {
    var startX: (T) -> Double = { throw IllegalStateException("startX must be specified") }
    var startY: (T) -> Double = { throw IllegalStateException("startX must be specified") }
    var controlX: (T) -> Double = { throw IllegalStateException("controlX must be specified") }
    var controlY: (T) -> Double = { throw IllegalStateException("controlY must be specified") }
    var endX: (T) -> Double = { throw IllegalStateException("endX must be specified") }
    var endY: (T) -> Double = { throw IllegalStateException("endX must be specified") }

    fun startX(x: Double) = startX { x }
    fun startX(x: (T) -> Double): QuadCurve<T> {
        startX = x
        return this
    }

    fun startY(y: Double) = startY { y }
    fun startY(y: (T) -> Double): QuadCurve<T> {
        startY = y
        return this
    }

    fun controlX(x: Double) = controlX { x }
    fun controlX(x: (T) -> Double): QuadCurve<T> {
        controlX = x
        return this
    }

    fun controlY(y: Double) = controlY { y }
    fun controlY(y: (T) -> Double): QuadCurve<T> {
        controlY = y
        return this
    }

    fun endX(x: Double) = endX { x }
    fun endX(x: (T) -> Double): QuadCurve<T> {
        endX = x
        return this
    }

    fun endY(y: Double) = endY { y }
    fun endY(y: (T) -> Double): QuadCurve<T> {
        endY = y
        return this
    }

    override operator fun invoke(d: T): javafx.scene.shape.Shape {
        val curve = javafx.scene.shape.QuadCurve()
        with(curve) {
            startX = startX(d)
            startY = startY(d)
            controlX = controlX(d)
            controlY = controlY(d)
            endX = endX(d)
            endY = endY
        }

        apply(d, curve)

        return curve
    }
}