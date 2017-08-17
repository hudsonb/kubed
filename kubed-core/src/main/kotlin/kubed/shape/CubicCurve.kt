package kubed.shape

class CubicCurve<T> : Shape<CubicCurve<T>, T>() {
    var startX: (T, Int) -> Double = { _, _ -> throw IllegalStateException("startX must be specified") }
    var startY: (T, Int) -> Double = { _, _ -> throw IllegalStateException("startX must be specified") }
    var controlX1: (T, Int) -> Double = { _, _ -> throw IllegalStateException("startX must be specified") }
    var controlY1: (T, Int) -> Double = { _, _ -> throw IllegalStateException("startX must be specified") }
    var controlX2: (T, Int) -> Double = { _, _ -> throw IllegalStateException("startX must be specified") }
    var controlY2: (T, Int) -> Double = { _, _ -> throw IllegalStateException("startX must be specified") }
    var endX: (T, Int) -> Double = { _, _ -> throw IllegalStateException("startX must be specified") }
    var endY: (T, Int) -> Double = { _, _ -> throw IllegalStateException("startX must be specified") }

    fun startX(x: Double) = startX { _, _ -> x }
    fun startX(x: (T, Int) -> Double): CubicCurve<T> {
        startX = x
        return this
    }

    fun startY(y: Double) = startY { _, _ -> y }
    fun startY(y: (T, Int) -> Double): CubicCurve<T> {
        startY = y
        return this
    }

    fun controlX1(x: Double) = controlX1 { _, _ -> x }
    fun controlX1(x: (T, Int) -> Double): CubicCurve<T> {
        controlX1 = x
        return this
    }

    fun controlY1(y: Double) = controlY1 { _, _ -> y }
    fun controlY1(y: (T, Int) -> Double): CubicCurve<T> {
        controlY1 = y
        return this
    }

    fun controlX2(x: Double) = controlX2 { _, _ -> x }
    fun controlX2(x: (T, Int) -> Double): CubicCurve<T> {
        controlX2 = x
        return this
    }

    fun controlY2(y: Double) = controlY2 { _, _ -> y }
    fun controlY2(y: (T, Int) -> Double): CubicCurve<T> {
        controlY2 = y
        return this
    }

    fun endX(x: Double) = endX { _, _ -> x }
    fun endX(x: (T, Int) -> Double): CubicCurve<T> {
        endX = x
        return this
    }

    fun endY(y: Double) = endY { _, _ -> y }
    fun endY(y: (T, Int) -> Double): CubicCurve<T> {
        endY = y
        return this
    }

    override operator fun invoke(d: T, i: Int): javafx.scene.shape.Shape {
        val curve = javafx.scene.shape.CubicCurve()
        with(curve) {
            startX = startX(d, i)
            startY = startY(d, i)
            controlX1 = controlX1(d, i)
            controlY1 = controlY1(d, i)
            controlX2 = controlX2(d, i)
            controlY2 = controlY2(d, i)
            endX = endX(d, i)
            endY = endY
        }

        apply(d, i, curve)

        return curve
    }
}