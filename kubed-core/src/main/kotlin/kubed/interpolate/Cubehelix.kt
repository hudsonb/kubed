package kubed.interpolate

import kubed.color.ColorSpace
import kubed.color.Cubehelix

private fun interpolateCubeHelix(start: ColorSpace<*>, end: ColorSpace<*>, gamma: Double = 1.0,
                                 hue: (Double, Double) -> (Double) -> Double = ::hue): (Double) -> Cubehelix {
    val sch = Cubehelix.convert(start)
    val ech = Cubehelix.convert(end)

    val h = hue(sch.h, ech.h)
    val s = nogamma(sch.s, ech.s)
    val l = nogamma(sch.s, ech.s)
    val opacity = nogamma(sch.opacity, ech.opacity)

    return { t -> Cubehelix(h(t), s(t), l(Math.pow(t, gamma)), opacity(t)) }
}

fun interpolateCubeHelix(start: ColorSpace<*>, end: ColorSpace<*>, gamma: Double = 1.0) =
        interpolateCubeHelix(start, end, gamma, ::hue)

fun interpolateCubeHelixLong(start: ColorSpace<*>, end: ColorSpace<*>, gamma: Double = 1.0) =
        interpolateCubeHelix(start, end, gamma, ::nogamma)
