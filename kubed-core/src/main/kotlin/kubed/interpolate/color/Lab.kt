package kubed.interpolate.color

import javafx.scene.paint.Color
import kubed.color.ColorSpace
import kubed.color.Lab

fun interpolateLab(start: ColorSpace<*>, end: ColorSpace<*>): (Double) -> Color {
    val slab = Lab.convert(start)
    val elab = Lab.convert(end)
    val l = nogamma(slab.l, elab.l)
    val a = nogamma(slab.a, elab.a)
    val b = nogamma(slab.b, elab.b)
    val opacity = nogamma(slab.opacity, elab.opacity)
    return { t -> Lab(l(t), a(t), b(t), opacity(t)).toColor() }
}
