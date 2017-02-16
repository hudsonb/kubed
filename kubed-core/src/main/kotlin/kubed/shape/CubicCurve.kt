package kubed.shape

class CubicCurve<T> : Shape<CubicCurve<T>, T>() {
    var startX: (T) -> Double = { throw IllegalStateException("startX must be specified") }
    var startY: (T) -> Double = { throw IllegalStateException("startX must be specified") }
    var controlX1: (T) -> Double = { throw IllegalStateException("startX must be specified") }
    var controlY1: (T) -> Double = { throw IllegalStateException("startX must be specified") }
    var controlX2: (T) -> Double = { throw IllegalStateException("startX must be specified") }
    var controlY2: (T) -> Double = { throw IllegalStateException("startX must be specified") }
    var endX: (T) -> Double = { throw IllegalStateException("startX must be specified") }
    var endY: (T) -> Double = { throw IllegalStateException("startX must be specified") }

    fun startX(x: Double) = startX { x }
    fun startX(x: (T) -> Double): CubicCurve<T> {
        startX = x
        return this
    }

    fun startY(y: Double) = startY { y }
    fun startY(y: (T) -> Double): CubicCurve<T> {
        startY = y
        return this
    }

    fun controlX1(x: Double) = controlX1 { x }
    fun controlX1(x: (T) -> Double): CubicCurve<T> {
        controlX1 = x
        return this
    }

    fun controlY1(y: Double) = controlY1 { y }
    fun controlY1(y: (T) -> Double): CubicCurve<T> {
        controlY1 = y
        return this
    }

    fun controlX2(x: Double) = controlX2 { x }
    fun controlX2(x: (T) -> Double): CubicCurve<T> {
        controlX2 = x
        return this
    }

    fun controlY2(y: Double) = controlY2 { y }
    fun controlY2(y: (T) -> Double): CubicCurve<T> {
        controlY2 = y
        return this
    }

    fun endX(x: Double) = endX { x }
    fun endX(x: (T) -> Double): CubicCurve<T> {
        endX = x
        return this
    }

    fun endY(y: Double) = endY { y }
    fun endY(y: (T) -> Double): CubicCurve<T> {
        endY = y
        return this
    }

    override operator fun invoke(d: T): javafx.scene.shape.Shape {
        val curve = javafx.scene.shape.CubicCurve()
        with(curve) {
            startX = startX(d)
            startY = startY(d)
            controlX1 = controlX1(d)
            controlY1 = controlY1(d)
            controlX2 = controlX2(d)
            controlY2 = controlY2(d)
            endX = endX(d)
            endY = endY
        }

        apply(d, curve)

        return curve
    }
}