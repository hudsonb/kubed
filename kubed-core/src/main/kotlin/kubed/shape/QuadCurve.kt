package kubed.shape

class QuadCurve<T> : Shape<QuadCurve<T>, T>() {
    var startX: (T, Int) -> Double = { _, _ -> throw IllegalStateException("startX must be specified") }
    var startY: (T, Int) -> Double = { _, _ -> throw IllegalStateException("startX must be specified") }
    var controlX: (T, Int) -> Double = { _, _ -> throw IllegalStateException("controlX must be specified") }
    var controlY: (T, Int) -> Double = { _, _ -> throw IllegalStateException("controlY must be specified") }
    var endX: (T, Int) -> Double = { _, _ -> throw IllegalStateException("endX must be specified") }
    var endY: (T, Int) -> Double = { _, _ -> throw IllegalStateException("endX must be specified") }

    fun startX(x: Double) = startX { _, _ -> x }
    fun startX(x: (T, Int) -> Double): QuadCurve<T> {
        startX = x
        return this
    }

    fun startY(y: Double) = startY { _, _ -> y }
    fun startY(y: (T, Int) -> Double): QuadCurve<T> {
        startY = y
        return this
    }

    fun controlX(x: Double) = controlX { _, _ -> x }
    fun controlX(x: (T, Int) -> Double): QuadCurve<T> {
        controlX = x
        return this
    }

    fun controlY(y: Double) = controlY { _, _ -> y }
    fun controlY(y: (T, Int) -> Double): QuadCurve<T> {
        controlY = y
        return this
    }

    fun endX(x: Double) = endX { _, _ -> x }
    fun endX(x: (T, Int) -> Double): QuadCurve<T> {
        endX = x
        return this
    }

    fun endY(y: Double) = endY { _, _ -> y }
    fun endY(y: (T, Int) -> Double): QuadCurve<T> {
        endY = y
        return this
    }

    override operator fun invoke(d: T, i: Int): javafx.scene.shape.Shape {
        val curve = javafx.scene.shape.QuadCurve()
        with(curve) {
            startX = startX(d, i)
            startY = startY(d, i)
            controlX = controlX(d, i)
            controlY = controlY(d, i)
            endX = endX(d, i)
            endY = endY
        }

        apply(d, i, curve)

        return curve
    }
}