package kubed.color

import javafx.scene.paint.Color
import kubed.util.isTruthy

/**
 * Convert to Hsl color space
 */
fun Rgb.hsl(): Hsl {
    val r = this.r / 255.0
    val g = this.g / 255.0
    val b = this.b / 255.0
    val min = Math.min(r, Math.min(g, b))
    val max = Math.max(r, Math.max(g, b))

    var h = Double.NaN
    var s = max - min
    val l = (max + min) / 2.0

    if(s.isTruthy()) {
        h = when {
            r == max -> (g - b) / s + if(g > b) 6 else 0
            g == max -> (b - r) / s + 2
            else -> (r - g) / s + 4
        }

        s /= if(l < 0.5) max + min else 2 - max - min
        h *= 60
    }
    else
        s = if(l > 0 && l < 1) 0.0 else h

    return Hsl(h, s, l, opacity)
}

class Hsl(var h: Double, var s: Double, var l: Double, var opacity: Double = 1.0) : ColorSpace<Hsl> {
    companion object {
        @JvmStatic
        fun convert(value: Any): Hsl = when(value)  {
            is Hsl -> Hsl(value.h, value.s, value.l, value.opacity)
            is ColorSpace<*> -> value.rgb().hsl()
            is Color -> value.hsl()
            is String -> Color.web(value).hsl()
            else -> throw IllegalArgumentException("Unable to coerce " + value.javaClass.name + " to Hsl")
        }
    }

    override fun rgb(): Rgb {
        val h = this.h % 360.0 + (if(h < 0.0) 360.0 else 0.0)
        val s2 = if(h.isNaN() || s.isNaN()) 0.0 else s
        val m2 = l + (if(l < 0.5) l else 1.0 - l) * s2
        val m1 = 2.0 * l - m2

        return Rgb(hsl2rgb(if(h >= 240.0) h - 240.0 else h + 120.0, m1, m2),
                   hsl2rgb(h, m1, m2),
                   hsl2rgb(if(h < 120.0) h + 240.0 else h - 120.0, m1, m2))
    }

    override fun brighter(k: Double): Hsl {
        val t = if(k == BRIGHTER) BRIGHTER else Math.pow(BRIGHTER, k)
        return Hsl(h, s, l * t, opacity)
    }

    override fun darker(k: Double): Hsl {
        val t = if(k == DARKER) DARKER else Math.pow(DARKER, k)
        return Hsl(h, s, l * t, opacity)
    }

    /* From FvD 13.37, CSS Color Module Level 3 */
    private fun hsl2rgb(h: Double, m1: Double, m2: Double): Int {
        val t = when {
            h < 60.0 -> m1 + (m2 - m1) * h / 60.0
            h < 180.0 -> m2
            h < 240.0 -> m1 + (m2 - m1) * (240.0 - h) / 60.0
            else -> m1
        }

        return (t * 255.0).toInt()
    }
}