package kubed.scale

import kubed.interpolate.interpolateNumber

inline fun <reified R> scaleLinear() {
}

fun <D> scaleBand() = BandScale<D>()
fun <D> scaleBand(init: BandScale<D>.() -> Unit) = BandScale<D>().apply { init.invoke(this) }

fun <D, R> scaleOrdinal() = OrdinalScale<D, R>()
fun <D, R> scaleOrdinal(init: OrdinalScale<D, R>.() -> Unit) = OrdinalScale<D, R>().apply { init.invoke(this) }

inline fun <reified R> interpolator() = when {
    R::class == Number::class -> ::interpolateNumber
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