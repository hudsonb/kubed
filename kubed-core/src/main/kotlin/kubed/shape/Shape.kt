package kubed.shape

import javafx.scene.CacheHint
import javafx.scene.paint.Paint
import javafx.scene.shape.StrokeLineCap
import javafx.scene.shape.StrokeLineJoin
import javafx.scene.shape.StrokeType
import kubed.path.Context
import sun.misc.Cache

abstract class Shape<out S : Shape<S, T>, T> {
    var styleClasses: ((T) -> List<String>)? = null
    var opacity: ((T) -> Double)? = null
    var fill: ((T) -> Paint)? = null
    var smooth: ((T) -> Boolean)? = null
    var stroke: ((T) -> Paint?)? = null
    var strokeType: ((T) -> StrokeType)? = null
    var strokeWidth: ((T) -> Double)? = null
    var strokeLineJoin: ((T) -> StrokeLineJoin)? = null
    var strokeLineCap: ((T) -> StrokeLineCap)? = null
    var strokeMiterLimit: ((T) -> Double)? = null
    var strokeDashOffset: ((T) -> Double)? = null
    var strokeDashArray: ((T) -> FloatArray)? = null
    var translateX: ((T) -> Double)? = null
    var translateY: ((T) -> Double)? = null
    var translateZ: ((T) -> Double)? = null
    var cacheHint: ((T) -> CacheHint)? = null

    fun styleClasses(vararg styleClasses: String): S {
        if(styleClasses.isEmpty())
            this.styleClasses = null
        else {
            val list = styleClasses.toList()
            styleClasses { list }
        }
        return this as S
    }

    fun styleClasses(styleClasses: ((T) -> List<String>)): S {
        this.styleClasses = styleClasses
        return this as S
    }

    fun opacity(opacity: Double) = opacity { opacity }
    fun opacity(opacity: (T) -> Double): S {
        this.opacity = opacity
        return this as S
    }

    fun fill(paint: Paint) = fill { paint }
    fun fill(paint: (T) -> Paint): S {
        fill = paint
        return this as S
    }

    fun smooth(value: Boolean) = smooth { value }
    fun smooth(value: (T) -> Boolean): S {
        smooth = value
        return this as S
    }

    fun stroke(paint: Paint?) = stroke { paint }
    fun stroke(paint: (T) -> Paint?): S {
        stroke = paint
        return this as S
    }

    fun strokeType(type: StrokeType) = strokeType { type }
    fun strokeType(type: (T) -> StrokeType): S {
        strokeType = type
        return this as S
    }

    fun strokeWidth(width: Double) = strokeWidth { width }
    fun strokeWidth(width: (T) -> Double): S {
        strokeWidth = width
        return this as S
    }

    fun strokeLineJoin(join: StrokeLineJoin) = strokeLineJoin { join }
    fun strokeLineJoin(join: (T) -> StrokeLineJoin): S {
        strokeLineJoin = join
        return this as S
    }

    fun strokeLineCap(cap: StrokeLineCap)= strokeLineCap { cap }
    fun strokeLineCap(cap: (T) -> StrokeLineCap): S {
        strokeLineCap = cap
        return this as S
    }

    fun strokeMiterLimit(limit: Double) = strokeMiterLimit { limit }
    fun strokeMiterLimit(limit: (T) -> Double): S {
        strokeMiterLimit = limit
        return this as S
    }

    fun strokeDashOffset(offset: Double) = strokeDashOffset { offset }
    fun strokeDashOffset(offset: (T) -> Double): S {
        strokeDashOffset = offset
        return this as S
    }

    fun translateX(x: Double) = translateX { x }
    fun translateX(x: (T) -> Double): S {
        this.translateX = x
        return this as S
    }

    fun translateY(y: Double) = translateY { y }
    fun translateY(y: (T) -> Double): S {
        this.translateY = y
        return this as S
    }

    fun translateZ(z: Double) = translateZ { z }
    fun translateZ(z: (T) -> Double): S {
        this.translateZ = z
        return this as S
    }

    fun cacheHint(hint: CacheHint) = cacheHint { hint }
    fun cacheHint(hint: (T) -> CacheHint): S {
        this.cacheHint = hint
        return this as S
    }

    internal fun apply(d: T, shape: javafx.scene.shape.Shape) {
        shape.opacity = opacity?.invoke(d) ?: shape.opacity
        shape.fill = fill?.invoke(d) ?: shape.fill
        shape.isSmooth = smooth?.invoke(d) ?: shape.isSmooth
        shape.stroke = stroke?.invoke(d) ?: shape.stroke
        shape.strokeType = strokeType?.invoke(d) ?: shape.strokeType
        shape.strokeWidth = strokeWidth?.invoke(d) ?: shape.strokeWidth
        shape.strokeLineJoin = strokeLineJoin?.invoke(d) ?: shape.strokeLineJoin
        shape.strokeLineCap = strokeLineCap?.invoke(d) ?: shape.strokeLineCap
        shape.strokeMiterLimit = strokeMiterLimit?.invoke(d) ?: shape.strokeMiterLimit
        shape.strokeDashOffset = strokeDashOffset?.invoke(d) ?: shape.strokeDashOffset
        shape.translateX = translateX?.invoke(d) ?: shape.translateX
        shape.translateY = translateY?.invoke(d) ?: shape.translateY
        shape.translateZ = translateZ?.invoke(d) ?: shape.translateZ
        shape.cacheHint = cacheHint?.invoke(d) ?: shape.cacheHint
    }

    abstract operator fun invoke(d: T): javafx.scene.shape.Shape
}

abstract class PathShape<out S : PathShape<S, T>, T> : Shape<S, T>() {
    abstract fun generate(d: T): Context?

    override operator fun invoke(d: T): javafx.scene.shape.Shape {
        val context = generate(d) ?: throw IllegalStateException("Context must not be null")
        val path = context()
        apply(d, path)
        return path
    }
}
