package kubed.geo.projection

import kubed.geo.Position
import kubed.geo.math.SQRT2
import kubed.math.HALF_PI
import kotlin.math.PI

private const val sinuMollweidePhi = 0.7109889596207567

private const val sinuMollweideY = 0.0528035274542

fun sinumollweide() = sinumollweide {}
fun sinumollweide(init: MutableProjection.() -> Unit) = projection(SinuMollweide()) {
    rotateX = -20.0
    rotateY = -55.0
    scale = 164.263
    center = Position(0.0, -5.4036)

    init()
}

class SinuMollweide : InvertableProjector {
    private val mollweide = MollweideProjector(SQRT2 / HALF_PI, SQRT2, PI)
    private val sinusoidal = SinusoidalProjector()

    override fun invoke(lambda: Double, phi: Double) = when {
        phi > -sinuMollweidePhi -> {
            val coords = mollweide(lambda, phi)
            coords[1] += sinuMollweideY
            coords
        }
        else -> sinusoidal(lambda, phi)
    }

    override fun invert(x: Double, y: Double) = when {
        y > -sinuMollweidePhi -> mollweide.invert(x, y - sinuMollweideY)
        else -> sinusoidal.invert(x, y)
    }
}