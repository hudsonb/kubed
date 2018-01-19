package kubed.color

import javafx.scene.paint.Color
import kubed.math.toRadians
import kubed.util.isTruthy
import java.lang.Math.pow
import kotlin.math.*

private const val A = -0.14861
private const val B = 1.78277
private const val C = -0.29227
private const val D = -0.90649
private const val E = 1.97294
private const val ED = E * D
private const val EB = E * B
private const val BC_DA = B * C - D * A

/**
 * Returns a representation of this color in the Cubehelix color space
 */
fun Color.cubehelix(): Cubehelix = rgb().cubehelix()

fun Rgb.cubehelix(): Cubehelix {
    val r = this.r / 255.0
    val g = this.g / 255.0
    val b = this.b / 255.0

    val l = (BC_DA * b + ED * r - EB * g) / (BC_DA + ED - EB)
    val bl = b - l
    val k = (E * (g - l) - C * bl) / D

    val s = when(l) {
        0.0, 1.0 -> Double.NaN
        else -> sqrt(k * k + bl * bl) / (E * l * (1 - l))
    }

    val h = if(s.isTruthy()) atan2(k, bl).toRadians() - 120 else Double.NaN
    return Cubehelix(if(h < 0) h + 360 else h, s, l, opacity)
}

class Cubehelix(var h: Double, var s: Double, var l: Double, var opacity: Double = 1.0) : ColorSpace<Cubehelix> {
    companion object {
        @JvmStatic
        fun convert(value: Any): Cubehelix = when(value)  {
            is Cubehelix -> Cubehelix(value.h, value.s, value.l, value.opacity)
            is ColorSpace<*> -> value.rgb().cubehelix()
            is Color -> value.cubehelix()
            is String -> Color.web(value).cubehelix()

            else -> throw IllegalArgumentException("Unable to coerce " + value.javaClass.name + " to Cubehelix")
        }
    }

    override fun rgb(): Rgb {
        val h = if(h.isNaN()) 0.0 else (h + 120).toRadians()
        val a = if(s.isNaN()) 0.0 else s * l * (1 - l)
        val cosh = cos(h)
        val sinh = sin(h)

        return Rgb(min(255.0, 255 * (l + a * (A * cosh + B * sinh))).toInt(),
                   min(255.0, 255 * (l + a * (C * cosh + D * sinh))).toInt(),
                   min(255.0, 255 * (l + a * (E * cosh))).toInt(),
                   opacity)
    }

    override fun brighter(k: Double): Cubehelix {
        val t = if(k == BRIGHTER) BRIGHTER else pow(BRIGHTER, k)
        return Cubehelix(h, s, l * t, opacity)
    }

    override fun darker(k: Double): Cubehelix {
        val t = if(k == DARKER) DARKER else pow(DARKER, k)
        return Cubehelix(h, s, l * t, opacity)
    }
}
