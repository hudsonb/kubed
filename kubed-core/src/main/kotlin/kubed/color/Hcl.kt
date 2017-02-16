package kubed.color

import kubed.util.MoreMath

fun Rgb.hcl() = Hcl.convert(this)

class Hcl(val h: Double, var c: Double, var l: Double, var opacity: Double = 1.0) : ColorSpace<Hcl> {
    companion object {
        @JvmStatic
        fun convert(value: Any) = when(value) {
            is Hcl -> Hcl(value.h, value.c, value.l, value.opacity)
            else -> {
                try {
                    val lab = Lab.convert(value)
                    val h = Math.atan2(lab.b, lab.a) * MoreMath.RAD_2_DEG
                    Hcl(if(h < 0) h + 360 else h, Math.sqrt(lab.a * lab.a + lab.b * lab.b), lab.l, lab.opacity)
                }
                catch(e: Exception) {
                    throw IllegalArgumentException("Unable to coerce " + value.javaClass.name + " to Hcl")
                }
            }
        }
    }

    override fun rgb(): Rgb = Lab.convert(this).rgb()
    override fun brighter(k: Double): Hcl = Hcl(h, c, l + Kn * k, opacity)
    override fun darker(k: Double): Hcl = Hcl(h, c, l - Kn * k, opacity)
}
