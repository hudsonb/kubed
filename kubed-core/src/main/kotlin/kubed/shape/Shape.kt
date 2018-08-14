package kubed.shape

import javafx.beans.binding.DoubleExpression
import javafx.scene.CacheHint
import javafx.scene.Node
import javafx.scene.paint.Paint
import javafx.scene.shape.StrokeLineCap
import javafx.scene.shape.StrokeLineJoin
import javafx.scene.shape.StrokeType
import kubed.path.Context

abstract class Shape<out S : Shape<S, T>, T> {
    var styleClasses: ((T, Int) -> List<String>)? = null
    var opacity: ((T, Int) -> Double)? = null
    var fill: ((T, Int) -> Paint?)? = null
    var smooth: ((T, Int) -> Boolean)? = null
    var stroke: ((T, Int) -> Paint?)? = null
    var strokeType: ((T, Int) -> StrokeType)? = null
    var strokeWidth: ((T, Int) -> Double)? = null
    var strokeLineJoin: ((T, Int) -> StrokeLineJoin)? = null
    var strokeLineCap: ((T, Int) -> StrokeLineCap)? = null
    var strokeMiterLimit: ((T, Int) -> Double)? = null
    var strokeDashOffset: ((T, Int) -> Double)? = null
    var strokeDashArray: ((T, Int) -> FloatArray)? = null
    var layoutXProperty: ((T, Int) -> DoubleExpression)? = null
    var layoutX: ((T, Int) -> Double)? = null
    var layoutYProperty: ((T, Int) -> DoubleExpression)? = null
    var layoutY: ((T, Int) -> Double)? = null
    var translateX: ((T, Int) -> Double)? = null
    var translateXProperty: ((T, Int) -> DoubleExpression)? = null
    var translateY: ((T, Int) -> Double)? = null
    var translateYProperty: ((T, Int) -> DoubleExpression)? = null
    var translateZ: ((T, Int) -> Double)? = null
    var translateZProperty: ((T, Int) -> DoubleExpression)? = null
    var cache: ((T, Int) -> Boolean)? = null
    var cacheHint: ((T, Int) -> CacheHint)? = null
    var clip: ((T, Int) -> Node?)? = null

    fun styleClasses(vararg styleClasses: String): S {
        if(styleClasses.isEmpty())
            this.styleClasses = null
        else {
            val list = styleClasses.toList()
            styleClasses { _, _ -> list }
        }
        return this as S
    }

    fun styleClasses(styleClasses: ((T, Int) -> List<String>)): S {
        this.styleClasses = styleClasses
        return this as S
    }

    fun opacity(opacity: Double) = opacity { _, _ -> opacity }
    fun opacity(opacity: (T, Int) -> Double): S {
        this.opacity = opacity
        return this as S
    }

    fun fill(paint: Paint?) = fill { _, _ -> paint }
    fun fill(paint: (T, Int) -> Paint?): S {
        fill = paint
        return this as S
    }

    fun smooth(value: Boolean) = smooth { _, _ -> value }
    fun smooth(value: (T, Int) -> Boolean): S {
        smooth = value
        return this as S
    }

    fun stroke(paint: Paint?) = stroke { _, _ -> paint }
    fun stroke(paint: (T, Int) -> Paint?): S {
        stroke = paint
        return this as S
    }

    fun strokeType(type: StrokeType) = strokeType { _, _ -> type }
    fun strokeType(type: (T, Int) -> StrokeType): S {
        strokeType = type
        return this as S
    }

    fun strokeWidth(width: Double) = strokeWidth { _, _ -> width }
    fun strokeWidth(width: (T, Int) -> Double): S {
        strokeWidth = width
        return this as S
    }

    fun strokeLineJoin(join: StrokeLineJoin) = strokeLineJoin { _, _ -> join }
    fun strokeLineJoin(join: (T, Int) -> StrokeLineJoin): S {
        strokeLineJoin = join
        return this as S
    }

    fun strokeLineCap(cap: StrokeLineCap)= strokeLineCap { _, _ -> cap }
    fun strokeLineCap(cap: (T, Int) -> StrokeLineCap): S {
        strokeLineCap = cap
        return this as S
    }

    fun strokeMiterLimit(limit: Double) = strokeMiterLimit { _, _ -> limit }
    fun strokeMiterLimit(limit: (T, Int) -> Double): S {
        strokeMiterLimit = limit
        return this as S
    }

    fun strokeDashOffset(offset: Double) = strokeDashOffset { _, _ -> offset }
    fun strokeDashOffset(offset: (T, Int) -> Double): S {
        strokeDashOffset = offset
        return this as S
    }

    fun layoutXProperty(x: DoubleExpression) = layoutXProperty { _, _ -> x }
    fun layoutXProperty(x: (T, Int) -> DoubleExpression): S {
        this.layoutXProperty = x
        return this as S
    }

    fun layoutX(value: Double) = layoutX { _, _ -> value }
    fun layoutX(value: (T, Int) -> Double): S {
        this.layoutX = value
        return this as S
    }

    fun layoutYProperty(y: DoubleExpression) = layoutYProperty { _, _ -> y }
    fun layoutYProperty(y: (T, Int) -> DoubleExpression): S {
        this.layoutYProperty = y
        return this as S
    }

    fun layoutY(value: Double) = layoutY { _, _ -> value }
    fun layoutY(value: (T, Int) -> Double): S {
        this.layoutY = value
        return this as S
    }

    fun translateX(x: Double) = translateX { _, _ -> x }
    fun translateX(x: (T, Int) -> Double): S {
        this.translateX = x
        return this as S
    }

    fun translateXProperty(x: DoubleExpression) = translateXProperty { _, _ -> x }
    fun translateXProperty(x: (T, Int) -> DoubleExpression): S {
        this.translateXProperty = x
        return this as S
    }

    fun translateY(y: Double) = translateY { _, _ -> y }
    fun translateY(y: (T, Int) -> Double): S {
        this.translateY = y
        return this as S
    }

    fun translateYProperty(y: DoubleExpression) = translateYProperty { _, _ -> y }
    fun translateYProperty(y: (T, Int) -> DoubleExpression): S {
        this.translateYProperty = y
        return this as S
    }

    fun translateZ(z: Double) = translateZ { _, _ -> z }
    fun translateZ(z: (T, Int) -> Double): S {
        this.translateZ = z
        return this as S
    }

    fun cache(cache: Boolean) = cache { _, _ -> cache }
    fun cache(cache: (T, Int) -> Boolean): S {
        this.cache = cache
        return this as S
    }


    fun cacheHint(hint: CacheHint) = cacheHint { _, _ -> hint }
    fun cacheHint(hint: (T, Int) -> CacheHint): S {
        this.cacheHint = hint
        return this as S
    }

    fun clip(node: Node?) = clip { _, _ -> node }
    fun clip(node: (T, Int) -> Node?): S {
        this.clip = node
        return this as S
    }

    internal fun apply(d: T, i: Int, shape: javafx.scene.shape.Shape) {
        shape.opacity = opacity?.invoke(d, i) ?: shape.opacity

        if(fill != null) shape.fill = fill?.invoke(d, i)
        if(stroke != null) shape.stroke = stroke?.invoke(d, i)
        shape.isSmooth = smooth?.invoke(d, i) ?: shape.isSmooth
        shape.strokeType = strokeType?.invoke(d, i) ?: shape.strokeType
        shape.strokeWidth = strokeWidth?.invoke(d, i) ?: shape.strokeWidth
        shape.strokeLineJoin = strokeLineJoin?.invoke(d, i) ?: shape.strokeLineJoin
        shape.strokeLineCap = strokeLineCap?.invoke(d, i) ?: shape.strokeLineCap
        shape.strokeMiterLimit = strokeMiterLimit?.invoke(d, i) ?: shape.strokeMiterLimit
        shape.strokeDashOffset = strokeDashOffset?.invoke(d, i) ?: shape.strokeDashOffset

        if(layoutXProperty != null) {
            val p = layoutXProperty?.invoke(d, i)
            if(p != null) shape.layoutXProperty().bind(p)
            else shape.layoutXProperty().unbind()
        }
        else shape.layoutX = layoutX?.invoke(d, i) ?: shape.layoutX

        if(layoutYProperty != null) {
            val p = layoutYProperty?.invoke(d, i)
            if(p != null) shape.layoutYProperty().bind(p)
            else shape.layoutYProperty().unbind()
        }
        else shape.layoutY = layoutY?.invoke(d, i) ?: shape.layoutY

        if(translateXProperty != null) {
            val p = translateXProperty?.invoke(d, i)
            if(p != null) shape.translateXProperty().bind(p)
            else shape.translateXProperty().unbind()
        }
        else shape.translateX = translateX?.invoke(d, i) ?: shape.translateX

        if(translateYProperty != null) {
            val p = translateYProperty?.invoke(d, i)
            if(p != null) shape.translateYProperty().bind(p)
            else shape.translateYProperty().unbind()
        }
        else shape.translateY = translateY?.invoke(d, i) ?: shape.translateY

        if(translateZProperty != null) {
            val p = translateZProperty?.invoke(d, i)
            if(p != null) shape.translateZProperty().bind(p)
            else shape.translateZProperty().unbind()
        }
        else shape.translateZ = translateZ?.invoke(d, i) ?: shape.translateZ

        if(cache != null) shape.isCache = cache?.invoke(d, i) ?: shape.isCache
        shape.cacheHint = cacheHint?.invoke(d, i) ?: shape.cacheHint
        shape.clip = clip?.invoke(d, i) ?: shape.clip
    }

    operator fun invoke(d: T) = invoke(d, -1)
    abstract operator fun invoke(d: T, i: Int): javafx.scene.shape.Shape
}

abstract class PathShape<out S : PathShape<S, T>, T> : Shape<S, T>() {
    abstract fun generate(d: T, i: Int): Context?

    override operator fun invoke(d: T, i: Int): javafx.scene.shape.Shape {
        val context = generate(d, i) ?: throw IllegalStateException("Context must not be null")
        val path = context()
        apply(d, i, path)
        return path
    }
}
