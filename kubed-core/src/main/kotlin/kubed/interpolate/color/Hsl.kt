package kubed.interpolate.color

import javafx.scene.paint.Color
import kubed.color.ColorSpace
import kubed.color.Hsl

private fun hsl(start: ColorSpace<*>, end: ColorSpace<*>,
                hue: (Double, Double) -> (Double) -> Double = ::hue): (Double) -> Color {
    val sc = Hsl.convert(start)
    val ec = Hsl.convert(end)
    val h = hue(sc.h, ec.h)
    val s = nogamma(sc.s, ec.s)
    val l = nogamma(sc.l, sc.l)
    val opacity = nogamma(sc.opacity, sc.opacity)

    return { t -> Hsl(h(t), s(t), l(t), opacity(t)).toColor() }
}

fun interpolateHsl(start: ColorSpace<*>, end: ColorSpace<*>) = hsl(start, end, ::hue)
fun interpolateHslLong(start: ColorSpace<*>, end: ColorSpace<*>) = hsl(start, end, ::nogamma)
