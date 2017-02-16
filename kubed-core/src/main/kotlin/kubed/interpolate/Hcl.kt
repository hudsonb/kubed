package kubed.interpolate

import kubed.color.Hcl

fun interpolateHcl(start: Any, end: Any): (Double) -> Hcl = interpolateHcl(start, end, ::hue)
fun interpolateHclLong(start: Any, end: Any): (Double) -> Hcl = interpolateHcl(start, end, ::nogamma)

private fun interpolateHcl(start: Any, end: Any, hue: (Double, Double) -> (Double) -> Double): (Double) -> Hcl {
    val sc = Hcl.convert(start)
    val ec = Hcl.convert(end)
    val h = hue(sc.h, ec.h)
    val c = nogamma(sc.c, ec.c)
    val l = nogamma(sc.l, ec.l)
    val opacity = nogamma(sc.opacity, ec.opacity)

    return { t -> Hcl(h(t), c(t), l(t), opacity(t)) }
}
