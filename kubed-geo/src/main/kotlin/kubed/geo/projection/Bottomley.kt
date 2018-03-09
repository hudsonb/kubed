package kubed.geo.projection

import javafx.beans.InvalidationListener
import javafx.beans.property.SimpleDoubleProperty
import kubed.math.HALF_PI
import kubed.util.isTruthy
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun bottomley() = bottomley {}
fun bottomley(init: Bottomley.() -> Unit) = Bottomley().apply {
    scale = 158.837

    init()
}

class Bottomley : MutableProjection(BottomleyProjector(0.5)) {
    val fractionProperty = SimpleDoubleProperty(0.5)
    var fraction
        get() = fractionProperty.get()
        set(f) = fractionProperty.set(f)

    init {
        fractionProperty.addListener(InvalidationListener { projector = BottomleyProjector(fraction) })
    }
}

class BottomleyProjector(private val sinPsi: Double) : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val rho = HALF_PI - phi
        val eta = if(rho.isTruthy()) lambda * sinPsi * sin(rho) / rho else rho
        return doubleArrayOf(rho * sin(eta) / sinPsi, HALF_PI - rho * cos(eta))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val x1 = x * sinPsi
        val y1 = HALF_PI - y
        val rho = sqrt(x1 * x1 + y1 * y1)
        val eta = atan2(x1, y1)
        return doubleArrayOf((if(rho.isTruthy()) rho / sin(rho) else 1.0) * eta / sinPsi,
                             HALF_PI - rho)
    }
}