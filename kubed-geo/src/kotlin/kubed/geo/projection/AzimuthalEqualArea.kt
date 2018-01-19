package kubed.geo.projection

import kubed.math.asin
import kotlin.math.sqrt

fun azimuthalEqualArea() = azimuthalEqualArea {}
fun azimuthalEqualArea(init: Projection.() -> Unit) = projection(AzimuthalEqualArea()) {
    scale = 124.75
    clipAngle = 180 - 1e-3

    init()
}

class AzimuthalEqualArea: Azimuthal({ cxcy -> sqrt(2 / (1 + cxcy)) }, { z -> 2 * asin(z / 2) })