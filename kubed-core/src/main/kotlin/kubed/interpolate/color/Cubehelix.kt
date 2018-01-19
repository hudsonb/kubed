package kubed.interpolate.color

import javafx.scene.paint.Color
import kubed.color.ColorSpace
import kubed.color.Cubehelix
import kubed.color.cubehelix

private fun interpolateCubeHelix(start: ColorSpace<*>, end: ColorSpace<*>, gamma: Double = 1.0,
                                 hue: (Double, Double) -> (Double) -> Double = ::hue): (Double) -> Color {
    val sch = Cubehelix.convert(start)
    val ech = Cubehelix.convert(end)

    val h = hue(sch.h, ech.h)
    val s = nogamma(sch.s, ech.s)
    val l = nogamma(sch.l, ech.l)
    val opacity = nogamma(sch.opacity, ech.opacity)

    return { t -> Cubehelix(h(t), s(t), l(Math.pow(t, gamma)), opacity(t)).toColor() }
}

fun interpolateCubeHelix(start: Color, end: Color) =
        interpolateCubeHelix(start.cubehelix(), end.cubehelix(), 1.0, ::hue)

fun interpolateCubeHelix(start: ColorSpace<*>, end: ColorSpace<*>, gamma: Double = 1.0) =
        interpolateCubeHelix(start, end, gamma, ::hue)

fun interpolateCubeHelixLong(start: ColorSpace<*>, end: ColorSpace<*>, gamma: Double = 1.0) =
        interpolateCubeHelix(start, end, gamma, ::nogamma)
