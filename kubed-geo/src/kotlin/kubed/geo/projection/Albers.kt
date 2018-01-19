package kubed.geo.projection

fun albers() = conicEqualArea {
    parallels = doubleArrayOf(29.5, 45.5)
    scale = 1070.0
    translate = doubleArrayOf(480.0, 250.0)
    rotate = doubleArrayOf(96.0, 0.0)
    center = doubleArrayOf(-0.6, 38.7)
}