package kubed.geo.projection

import kubed.math.asin
import kotlin.math.cos
import kotlin.math.sin

fun hammer() = hammer { }
fun hammer(init: HammerProjection.() -> Unit) = hammer(2.0, 2.0, init)
fun hammer(a: Double, b: Double, init: HammerProjection.() -> Unit) = HammerProjection(a, b).apply {
    scale = 169.529
    init()
}

class HammerProjection(a: Double, private val b: Double = a) : MutableProjection(when {
    b == 1.0 -> AzimuthalEqualArea()
    b.isInfinite() -> HammerQuarticAuthical()
    else -> Hammer(a, b)
}) {
    var coefficient: Double
        get() = when(projector) {
            is AzimuthalEqualArea -> 1.0
            is HammerQuarticAuthical -> Double.POSITIVE_INFINITY
            else -> (projector as Hammer).b
        }
        set(value) {
            projector = when {
                b == 1.0 -> AzimuthalEqualArea()
                b.isInfinite() -> HammerQuarticAuthical()
                else -> Hammer(value, value)
            }
        }
}

class Hammer(val a: Double, val b: Double) : InvertableProjector {
    private val azimuthalEqualArea = AzimuthalEqualArea()

    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val coords = azimuthalEqualArea(lambda / b, phi)
        coords[0] *= a
        return coords
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val coords = azimuthalEqualArea(x / a, y)
        coords[0] *= b
        return coords
    }
}

class HammerQuarticAuthical : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double) = doubleArrayOf(
            lambda * cos(phi) / cos(phi / 2),
            2 * sin(phi / 2)
    )

    override fun invert(x: Double, y: Double): DoubleArray {
        val phi = 2 * asin(y / 2)
        return doubleArrayOf(x * cos(phi / 2) / cos(phi), phi)
    }
}