package kubed.geo.projection

import kubed.math.acos
import kubed.util.isTruthy
import kotlin.math.sin

fun azimuthalEquidistant() = azimuthalEqualArea {}
fun azimuthalEquidistant(init: Projection.() -> Unit) = projection(AzimuthalEquidistant()) {
    scale = 124.75
    clipAngle = 180 - 1e-3

    init()
}

private val scale = { cxcy: Double ->
    val c = acos(cxcy)
    if(c.isTruthy()) c / sin(c) else c
}

class AzimuthalEquidistant: Azimuthal(scale, { z -> z })