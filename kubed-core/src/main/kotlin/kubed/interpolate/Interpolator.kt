package kubed.interpolate

import javafx.scene.paint.Color
import kubed.color.ColorSpace
import kubed.color.Rgb

typealias Interpolator<T> = (Double) -> T
typealias Deinterpolator<T> = (T) -> Double

typealias InterpolatorFactory<T> = (T, T) -> Interpolator<T>
typealias DeinterpolatorFactory<T> = (T, T) -> Deinterpolator<T>

typealias PiecewiseInterpolatorFactory<T> = (List<Double>, List<T>, DeinterpolatorFactory<Double>, InterpolatorFactory<T>) -> Interpolator<T>
typealias PiecewiseDeinterpolatorFactory<T> = (List<T>, List<Double>, DeinterpolatorFactory<T>, InterpolatorFactory<Double>) -> Deinterpolator<T>

// Consider: Remove this? Seems pointless given you've gotta provide the types
//fun <T, R> interpolate(a: T, b: T): (Double) -> R {
//    return when(b) {
//        null, is Boolean -> { _ -> b }
//        is Number -> interpolateNumber((a as Number).toDouble(), b.toDouble()) as (Double) -> R
//        is Color -> interpolateRgb(a as Color, b) as (Double) -> R
//        is ColorSpace<*> -> interpolateRgb(a as ColorSpace<*>, b) as (Double) -> R
//        is String -> {
//            if(isColor(b))
//                interpolateRgb(Rgb.convert(a), Rgb.convert(b)) as (Double) -> R
//            else {
//                val bd = b.toDouble()
//
//                interpolateNumber((a as String).toDouble(), bd) as (Double) -> R
//            }
//
//        }
//        else -> throw IllegalArgumentException("An interpolator could not be found for the given type") // TODO: How do you get the type name?
//    }
//}

// Consider: Add this as an extension function to String
private fun isColor(str: String): Boolean {
    return try {
        Color.web(str)
        true
    }
    catch(e: Exception) {
        false
    }
}

/*fun <T> constant(b: T): (Double) -> T {
    return { b }
}*/

