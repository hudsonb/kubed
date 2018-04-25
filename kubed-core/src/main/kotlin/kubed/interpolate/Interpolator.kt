package kubed.interpolate

import javafx.scene.paint.Color

typealias Reinterpolator<T> = (Double) -> T
typealias Deinterpolator<T> = (T) -> Double

typealias ReinterpolatorFactory<T> = (T, T) -> Reinterpolator<T>
typealias DeinterpolatorFactory<T> = (T, T) -> Deinterpolator<T>

typealias PiecewiseReinterpolatorFactory<T> = (List<Double>, List<T>, DeinterpolatorFactory<Double>, ReinterpolatorFactory<T>) -> Reinterpolator<T>
typealias PiecewiseDeinterpolatorFactory<T> = (List<T>, List<Double>, DeinterpolatorFactory<T>, ReinterpolatorFactory<Double>) -> Deinterpolator<T>


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
    try {
        Color.web(str)
        return true
    }
    catch(e: Exception) {
        return false
    }
}

/*fun <T> constant(b: T): (Double) -> T {
    return { b }
}*/

