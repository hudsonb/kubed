package kubed.interpolate.color

import javafx.scene.paint.Color
import kubed.color.Cubehelix
import kubed.color.hsl

fun interpolateWarm() = interpolateCubeHelixLong(Cubehelix(-100.0, 0.75, 0.35), Cubehelix(80.0, 1.50, 0.8))
fun interpolateCool() = interpolateCubeHelixLong(Cubehelix(260.0, 0.75, 0.35), Cubehelix(80.0, 1.50, 0.8))

fun interpolateRainbow(): (Double) -> Color = { t: Double ->
    val t1 = if(t < 0 || t > 1) t - Math.floor(t) else t
    val ts = Math.abs(t1 - 0.5)
    hsl(360 * t1 - 100, 1.5 - 1.5 * ts, 0.8 - 0.9 * ts)
}