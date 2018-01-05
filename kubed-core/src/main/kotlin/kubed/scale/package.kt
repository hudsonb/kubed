package kubed.scale

import javafx.scene.paint.Color
import kubed.interpolate.interpolateNumber
import kubed.color.scheme.schemeCategory10
import kubed.color.scheme.schemeCategory20
import kubed.color.scheme.schemeCategory20b
import kubed.color.scheme.schemeCategory20c

inline fun <reified R> scaleLinear(): LinearScale<R> = scaleLinear {}
inline fun <reified R> scaleLinear(init: LinearScale<R>.() -> Unit): LinearScale<R> {
    // TODO: Default comparators

    val scale = LinearScale(interpolator<R>() as (R, R) -> (Double) -> R,
                            uninterpolator())
    scale.init()
    return scale
}

fun <D> scaleBand() = BandScale<D>()
fun <D> scaleBand(init: BandScale<D>.() -> Unit) = BandScale<D>().apply { init.invoke(this) }

fun <D, R> scaleOrdinal() = OrdinalScale<D, R>()
fun <D, R> scaleOrdinal(init: OrdinalScale<D, R>.() -> Unit) = OrdinalScale<D, R>().apply { init.invoke(this) }

fun <D> scalePoint() = PointScale<D>().apply { range(listOf(0.0, 1.0)) }
fun <D> scalePoint(init: PointScale<D>.() -> Unit) = PointScale<D>().apply {
    range(listOf(0.0, 1.0))
    init.invoke(this)
}

fun <R> scaleQuantile() = QuantileScale<R>()
fun <R> scaleQuantile(init: QuantileScale<R>.() -> Unit) = QuantileScale<R>().apply { init.invoke(this) }

fun <R> scaleQuantize() = QuantizeScale<R>()
fun <R> scaleQuantize(init: QuantizeScale<R>.() -> Unit) = QuantizeScale<R>().apply { init.invoke(this) }

fun <R> scaleThreshold() = ThresholdScale<R>()
fun <R> scaleThreshold(init: ThresholdScale<R>.() -> Unit) = ThresholdScale<R>().apply { init.invoke(this) }

// Consider: Should this be in the color package instead?
fun <D> scaleCategory10() = OrdinalScale<D, Color>().apply { range(schemeCategory10()) }
fun <D> scaleCategory20() = OrdinalScale<D, Color>().apply { range(schemeCategory20()) }
fun <D> scaleCategory20b() = OrdinalScale<D, Color>().apply { range(schemeCategory20b()) }
fun <D> scaleCategory20c() = OrdinalScale<D, Color>().apply { range(schemeCategory20c()) }

inline fun <reified R> interpolator() = when {
    R::class == Double::class -> ::interpolateNumber
    else -> throw IllegalArgumentException()
}

inline fun <reified R> uninterpolator(): ((R, R) -> (R) -> Double)? {
    return when(R::class) {
        Number::class -> { a: R, b: R ->
            val an = (a as Number).toDouble()
            val bn = (b as Number).toDouble()
            val b2 = bn - an
            when(b2) {
                -0.0, +0.0, Double.NaN -> { _ -> b2 }
                else -> { x: R -> ((x as Number).toDouble() - a.toDouble()) / b2 }
            }
        }
        else -> null
    }
}