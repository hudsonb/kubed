package kubed.geo.projection

import kotlin.math.PI

fun bromley() = bromley {}
fun bromley(init: MutableProjection.() -> Unit) = projection(MollweideProjector(1.0, 4 / PI, PI)).apply {
    scale = 152.63
    init()
}