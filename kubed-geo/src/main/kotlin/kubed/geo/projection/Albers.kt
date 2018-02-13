package kubed.geo.projection

import kubed.geo.Position

fun albers() = albers {}
fun albers(init: ConicProjection.() -> Unit) = conicEqualArea {
    parallels = doubleArrayOf(29.5, 45.5)
    scale = 1070.0
    translateX = 480.0
    translateY = 250.0
    rotateX = 96.0
    center = Position(-0.6, 38.7)

    init()
}