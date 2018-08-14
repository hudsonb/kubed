package kubed.geo.projection

import kubed.math.TAU
import kubed.math.asin
import kubed.math.toDegrees
import kubed.math.toRadians
import kubed.util.isTruthy
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private fun identity(x: Double, y: Double) = when {
    x > PI -> doubleArrayOf(x - TAU, y)
    x < -PI -> doubleArrayOf(x + TAU, y)
    else -> doubleArrayOf(x, y)
}

private fun rotationIdentity() = object : InvertableTransform {
    override fun invoke(lambda: Double, phi: Double) = identity(lambda, phi)
    override fun invert(x: Double, y: Double) = identity(x, y)
}

fun rotateRadians(deltaLambda: Double, deltaPhi: Double, deltaGamma: Double): Transform {
    val dl = deltaLambda % TAU
    return if(dl.isTruthy()) {
        if(deltaPhi.isTruthy() || deltaGamma.isTruthy()) compose(rotationLambda(deltaLambda), rotationPhiGamma(deltaPhi, deltaGamma))
        else rotationLambda(deltaLambda)
    }
    else if(deltaPhi.isTruthy() || deltaGamma.isTruthy()) rotationPhiGamma(deltaPhi, deltaGamma)
    else rotationIdentity()
}

fun forwardRotationLambda(deltaLambda: Double): (Double, Double) -> DoubleArray {
    return { lambda: Double, phi: Double ->
        val l = lambda + deltaLambda
        identity(l, phi)
    }
}

fun rotationLambda(deltaLambda: Double) = object : InvertableTransform {
    private val apply = forwardRotationLambda(deltaLambda)
    private val invert = forwardRotationLambda(-deltaLambda)

    override fun invoke(lambda: Double, phi: Double): DoubleArray = apply(lambda, phi)
    override fun invert(x: Double, y: Double): DoubleArray = invert.invoke(x, y)
}

fun rotationPhiGamma(deltaPhi: Double, deltaGamma: Double) = object : InvertableTransform {
    private val cosDeltaPhi = cos(deltaPhi)
    private val sinDeltaPhi = sin(deltaPhi)
    private val cosDeltaGamma = cos(deltaGamma)
    private val sinDeltaGamma = sin(deltaGamma)

    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        val cosPhi = cos(phi)
        val x = cos(lambda) * cosPhi
        val y = sin(lambda) * cosPhi
        val z = sin(phi)
        val k = z * cosDeltaPhi + x * sinDeltaPhi
        return doubleArrayOf(atan2(y * cosDeltaGamma - k * sinDeltaGamma, x * cosDeltaPhi - z * sinDeltaPhi),
                             asin(k * cosDeltaGamma + y * sinDeltaGamma))
    }

    override fun invert(x: Double, y: Double): DoubleArray {
        val cosPhi = cos(y)
        val cx = cos(x) * cosPhi
        val cy = sin(x) * cosPhi
        val z = sin(y)
        val k = z * cosDeltaGamma - y * sinDeltaGamma
        return doubleArrayOf(atan2(cy * cosDeltaGamma + z * sinDeltaGamma, cx * cosDeltaPhi + k * sinDeltaPhi),
                             asin(k * cosDeltaPhi - cx * sinDeltaPhi))
    }
}

fun rotation(rx: Double, ry: Double, rz: Double = 0.0) : InvertableTransform {
    val rotator = rotateRadians(rx.toRadians(), ry.toRadians(), rz.toRadians())

    return object : InvertableTransform {
        override fun invoke(lambda: Double, phi: Double): DoubleArray {
            val p = rotator(lambda.toRadians(), phi.toRadians())
            return doubleArrayOf(p[0].toDegrees(), p[1].toDegrees())
        }

        override fun invert(x: Double, y: Double): DoubleArray {
            rotator as InvertableTransform
            val p = rotator.invert(x.toRadians(), y.toRadians())
            return doubleArrayOf(p[0].toDegrees(), p[1].toDegrees())
        }
    }
}