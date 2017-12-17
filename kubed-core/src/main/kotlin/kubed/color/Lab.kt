package kubed.color

import javafx.scene.paint.Color
import kubed.util.MoreMath
import kubed.util.isTruthy

internal const val Kn = 18.0
internal const val Xn = 0.950470 // D65 standard referent
internal const val Yn = 1.0
internal const val Zn = 1.088830
private const val T0 = 4.0 / 29.0
private const val T1 = 6.0 / 29.0
private const val T2 = 3.0 * T1 * T1
private const val T3 = T1 * T1 * T1

fun Rgb.lab(): Lab {
    val b = rgb2xyz(r)
    val a = rgb2xyz(g)
    val l = rgb2xyz(this.b)
    val x = xyz2lab((0.4124564 * b + 0.3575761 * a + 0.1804375 * l) / Xn)
    val y = xyz2lab((0.2126729 * b + 0.7151522 * a + 0.0721750 * l) / Yn)
    val z = xyz2lab((0.0193339 * b + 0.1191920 * a + 0.9503041 * l) / Zn)
    return Lab(116 * y - 16, 500 * (x - y), 200 * (y - z), opacity)
}

internal fun rgb2xyz(x: Int): Double {
    val x2 = x / 255.0
    return if(x2 <= 0.04045) x2 / 12.92 else Math.pow((x2 + 0.055) / 1.055, 2.4)
}

internal fun xyz2lab(t: Double): Double {
    return when {
        t > T3 -> Math.pow(t, 1.0 / 3.0)
        else -> t / T2 + T0
    }
}

class Lab(val l: Double, val a: Double, val b: Double, val opacity: Double = 1.0) : ColorSpace<Lab> {
    companion object {
        @JvmStatic
        fun convert(value: Any): Lab = when(value)  {
            is Lab -> Lab(value.l, value.a, value.b, value.opacity)
            is Hcl -> {
                val h = value.h * MoreMath.DEG_2_RAD
                Lab(value.l, Math.cos(h) * value.c, Math.sin(h) * value.c, value.opacity)
            }
            is ColorSpace<*> -> value.rgb().lab()
            is Color -> value.lab()
            is String -> Color.web(value).lab()
            else -> throw IllegalArgumentException("Unable to coerce " + value.javaClass.name + " to Lab")
        }
    }

    override fun rgb(): Rgb {
        var y = (l + 16) / 116.0
        var x = if(a.isNaN()) y else y + a / 500.0
        var z = if(b.isNaN()) y else y - b / 200.0

        y = Yn * lab2xyz(y)
        x = Xn * lab2xyz(x)
        z = Zn * lab2xyz(z)

        return Rgb(xyz2rgb(3.2404542 * x - 1.571385 * y - 0.4985314 * z).toInt(),  // D65 -> sRGB
                   xyz2rgb(-0.9692660 * x + 1.8760108 * y + 0.0415560 * z).toInt(),
                   xyz2rgb( 0.0556434 * x - 0.2040259 * y + 1.0572252 * z).toInt(),
                   opacity)
    }

    override fun brighter(k: Double) = Lab(l + Kn * k, a, b, opacity)
    override fun darker(k: Double) = Lab(l - Kn * k, a, b, opacity)

    private fun lab2xyz(t: Double): Double {
        return when {
            t > T1 -> t * t * t
            else -> T2 * (t - T0)
        }
    }

    private fun xyz2rgb(x: Double): Double {
        return 255.0 * (if(x <= 0.0031308) 12.92 * x else 1.055 * Math.pow(x, 1.0 / 2.4) - 0.055)
    }
}