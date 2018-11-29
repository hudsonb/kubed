package kubed.scale

import javafx.scene.paint.Color
import kubed.interpolate.interpolateNumber
import kubed.color.scheme.schemeCategory10
import kubed.color.scheme.schemeCategory20
import kubed.color.scheme.schemeCategory20b
import kubed.color.scheme.schemeCategory20c
import kubed.interpolate.DeinterpolatorFactory
import kubed.interpolate.ReinterpolatorFactory
import kubed.interpolate.color.interpolateRgb
import kotlin.reflect.full.isSubclassOf

inline fun <reified R> scaleLinear(noinline interpolate: ReinterpolatorFactory<R>? = null,//((R, R) -> (Double) -> R)
                                   noinline uninterpolate: DeinterpolatorFactory<R>? = null,
                                   rangeComparator: Comparator<R>? = null): LinearScale<R> = scaleLinear(interpolate, uninterpolate, rangeComparator) {}
inline fun <reified R> scaleLinear(noinline interpolate: ReinterpolatorFactory<R>? = null,
                                   noinline uninterpolate: DeinterpolatorFactory<R>? = null,
                                   rangeComparator: Comparator<R>? = null,
                                   init: LinearScale<R>.() -> Unit): LinearScale<R> {
    val scale = LinearScale(interpolate ?: interpolator(),
                                            uninterpolate ?: uninterpolator(),
                                    rangeComparator ?: defaultComparator())
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

@Suppress("UNCHECKED_CAST")
inline fun <reified R> interpolator() = when {
    R::class.isSubclassOf(Number::class) -> ::interpolateNumber as ReinterpolatorFactory<R>
    R::class == Color::class -> ::interpolateRgb as ReinterpolatorFactory<R>
    else -> throw IllegalArgumentException("No default ReinterpolatorFactory for ${R::class.qualifiedName}")
}

inline fun <reified R> uninterpolator(): ((R, R) -> (R) -> Double)? {
    return when {
        R::class.isSubclassOf(Number::class) -> { a: R, b: R ->
            val an = (a as Number).toDouble()
            val bn = (b as Number).toDouble()
            val d = bn - an
            when {
                d == -0.0 || d == +0.0 || d.isNaN() -> { _ -> d }
                else -> { x: R -> ((x as Number).toDouble() - an) / d }
            }
        }
        else -> null
    }
}

inline fun <reified R> defaultComparator(): Comparator<R>? = when {
    R::class.isSubclassOf(Comparable::class) -> naturalOrder<Nothing>() as Comparator<R>
    else -> null
}