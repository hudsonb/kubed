package kubed.geo.projection

import javafx.beans.InvalidationListener
import javafx.beans.property.SimpleDoubleProperty
import kubed.math.EPSILON
import kubed.math.HALF_PI
import kubed.math.toDegrees
import kubed.math.toRadians
import kubed.util.isFalsy
import kubed.util.isTruthy
import java.lang.Math.tan
import kotlin.math.*

fun airy() = airy {}
fun airy(init: AiryProjection.() -> Unit) = AiryProjection().apply {
    scale = 179.976
    clipAngle = 147.0

    init()
}

class AiryProjector(private val beta: Double) : InvertableProjector {
    private val tanBeta2 = tan(beta / 2)
    private val b = 2 * ln(cos(beta / 2)) / (tanBeta2 * tanBeta2)

    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val cosx = cos(lambda)
        val cosy = cos(phi)
        val siny = sin(phi)
        val cosz = cosx * cosy

        val t = if((1 - cosz).isTruthy()) ln((1 + cosz) / 2) / (1 - cosz) else -0.5
        val k = -(t + b / (1 + cosz))
        return doubleArrayOf(k * cosy * sin(lambda), k * siny)
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val r = sqrt(x * x + y * y)
        if(r.isFalsy()) return doubleArrayOf(0.0, 0.0)

        var delta: Double
        var z = -beta / 2
        var i = 50
        do {
            val z2 = z / 2
            val cosz2 = cos(z2)
            val sinz2 = sin(z2)
            val tanz2 = tan(z2)
            val lnsecz2 = ln(1 / cosz2)
            delta = (2 / tanz2 * lnsecz2 - b * tanz2 - r) / (-lnsecz2 / (sinz2 * sinz2) + 1 - b / (2 * cosz2 * cosz2))
            z -= delta
        } while(abs(delta) > EPSILON && --i > 0)

        val sinz = sin(z)
        return doubleArrayOf(atan2(x * sinz, r * cos(z)), asin(y * sinz / r))
    }
}

class AiryProjection : MutableProjection(AiryProjector(HALF_PI)) {
    val radiusProperty = SimpleDoubleProperty(HALF_PI.toDegrees())
    var radius
        get() = radiusProperty.get()
        set(f) = radiusProperty.set(f)

    init {
        radiusProperty.addListener(InvalidationListener { projector = AiryProjector(radius.toRadians()) })
    }
}