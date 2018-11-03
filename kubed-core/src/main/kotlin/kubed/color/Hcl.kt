package kubed.color

import javafx.scene.paint.Color
import kubed.math.toDegrees
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Returns a representation of this color in the Hsl color space
 */
fun Color.hcl(): Hcl = rgb().hcl()

fun Rgb.hcl() = Hcl.convert(this)

data class Hcl(val h: Double, var c: Double, var l: Double, var opacity: Double = 1.0) : ColorSpace<Hcl> {
    companion object {
        @JvmStatic
        fun convert(value: Any) = when(value) {
            is Hcl -> Hcl(value.h, value.c, value.l, value.opacity)
            else -> {
                try {
                    val lab = Lab.convert(value)
                    val h = atan2(lab.b, lab.a).toDegrees()
                    Hcl(if(h < 0) h + 360 else h, sqrt(lab.a * lab.a + lab.b * lab.b), lab.l, lab.opacity)
                }
                catch(e: Exception) {
                    throw IllegalArgumentException("Unable to coerce " + value.javaClass.name + " to Hcl")
                }
            }
        }
    }

    override fun rgb(): Rgb = Lab.convert(this).rgb()
    override fun brighter(k: Double) = Hcl(h, c, l + K * k, opacity)
    override fun darker(k: Double) = Hcl(h, c, l - K * k, opacity)
}
