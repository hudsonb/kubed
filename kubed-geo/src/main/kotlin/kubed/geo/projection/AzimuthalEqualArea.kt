package kubed.geo.projection

import kubed.geo.math.sqrt
import kubed.math.asin

fun azimuthalEqualArea() = azimuthalEqualArea {}
fun azimuthalEqualArea(init: MutableProjection.() -> Unit) = projection(AzimuthalEqualArea()) {
    scale = 124.75
    clipAngle = 180 - 1e-3

    init()
}

class AzimuthalEqualArea: Azimuthal({ cxcy -> sqrt(2 / (1 + cxcy)) }, { z -> 2 * asin(z / 2) })