package kubed.color

import javafx.scene.paint.Color
import kubed.math.toRadians
import java.lang.Math.pow
import kotlin.math.cos
import kotlin.math.sin

internal const val K = 18.0
internal const val Xn = 0.96422
internal const val Yn = 1.0
internal const val Zn = 0.82521
private const val T0 = 4.0 / 29.0
private const val T1 = 6.0 / 29.0
private const val T2 = 3.0 * T1 * T1
private const val T3 = T1 * T1 * T1

/**
 * Returns a representation of this color in the CIELAB color space
 */
fun Color.lab(): Lab = rgb().lab()

fun Rgb.lab(): Lab {
    val r = rgb2lrgb(r)
    g = rgb2lrgb(g)
    b = rgb2lrgb(b)
    val y = xyz2lab((0.2225045 * r + 0.7168786 * g + 0.0606169 * b) / Yn)
    val x: Double
    val z: Double
    if(r == g && g == b) {
        x = y
        z = y
    }
    else {
        x = xyz2lab((0.4360747 * r + 0.3850649 * g + 0.1430804 * b) / Xn)
        z = xyz2lab((0.0139322 * r + 0.0971045 * g + 0.7141733 * b) / Zn)
    }
    return Lab(116 * y - 16, 500 * (x - y), 200 * (y - z), opacity)
}

private fun rgb2lrgb(x: Double): Double {
    val x2 = x / 255
    return if(x2 <= 0.04045) x2 / 12.92 else pow((x2 + 0.055) / 1.055, 2.4)
}


private fun xyz2lab(t: Double): Double {
    return when {
        t > T3 -> pow(t, 1.0 / 3.0)
        else -> t / T2 + T0
    }
}

class Lab(val l: Double, val a: Double, val b: Double, val opacity: Double = 1.0) : ColorSpace<Lab> {
    companion object {
        @JvmStatic
        fun convert(value: Any): Lab = when(value)  {
            is Lab -> Lab(value.l, value.a, value.b, value.opacity)
            is Hcl -> {
                val h = value.h.toRadians()
                Lab(value.l, cos(h) * value.c, sin(h) * value.c, value.opacity)
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

        x = Xn * lab2xyz(x)
        y = Yn * lab2xyz(y)
        z = Zn * lab2xyz(z)

        return Rgb(lrgb2rgb(3.1338561 * x - 1.6168667 * y - 0.4906146 * z),
                   lrgb2rgb(-0.9787684 * x + 1.9161415 * y + 0.0334540 * z),
                   lrgb2rgb(0.0719453 * x - 0.2289914 * y + 1.4052427 * z),
                   opacity)
    }

    override fun brighter(k: Double) = Lab(l + K * k, a, b, opacity)
    override fun darker(k: Double) = Lab(l - K * k, a, b, opacity)

    private fun lab2xyz(t: Double) = when {
        t > T1 -> t * t * t
        else -> T2 * (t - T0)
    }

    private fun lrgb2rgb(x: Double) = 255 * (if(x <= 0.0031308) 12.92 * x else 1.055 * Math.pow(x, 1 / 2.4) - 0.055)
}