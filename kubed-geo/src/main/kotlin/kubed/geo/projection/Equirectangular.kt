package kubed.geo.projection

fun equirectangular() = equirectangular {}
fun equirectangular(init: MutableProjection.() -> Unit) = projection(EquirectangularProjector()) {
    scale = 152.63

    init()
}

class EquirectangularProjector : InvertableProjector {
    override fun invoke(lambda: Double, phi: Double) = doubleArrayOf(lambda, phi)
    override fun invert(x: Double, y: Double) = doubleArrayOf(x, y)
}