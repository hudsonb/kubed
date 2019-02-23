package kubed.geo.projection

import kubed.geo.Position
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun bertin1953() = bertin1953 {}
fun bertin1953(init: MutableProjection.() -> Unit) = projection(Bertin1953()) {
    rotateX = -16.5
    rotateY = -42.0
    scale = 176.57
    center = Position(7.93, 0.09)

    init()
}


private const val fu = 1.4
private const val k = 12

class Bertin1953 : Projector {
    private val hammer = Hammer(1.68, 2.0)

    override fun invoke(lambda: Double, phi: Double): DoubleArray {
        var l = lambda
        var p = phi

        if(l + p < -fu) {
            val u = (l - p + 1.6) * (l + p + fu) / 8
            l += u
            p -= 0.8 * u * sin(p + PI / 2)
        }

        val r = hammer(l, p)
        val d = (1 - cos(l * p)) / k
        if(r[1] < 0)
            r[0] *= 1 + d

        if(r[1] > 0)
            r[1] *= 1 + d / 1.5 * r[0] * r[0]

        return r
    }
}