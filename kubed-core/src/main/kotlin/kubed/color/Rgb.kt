package kubed.color

import javafx.scene.paint.Color
import kubed.math.clamp
import java.lang.Math.pow

/**
 * Returns a representation of this color in the Rgb color space
 */
fun Color.rgb(): Rgb = Rgb(red * 255,green * 255, blue * 255, opacity)

class Rgb(var r: Double, var g: Double, var b: Double, var opacity: Double = 1.0) : ColorSpace<Rgb> {
    companion object {
        @JvmStatic
        fun convert(value: Any): Rgb = when(value)  {
            is Rgb -> Rgb(value.r, value.g, value.b, value.opacity)
            is ColorSpace<*> -> value.rgb()
            is Color -> value.rgb()
            is String -> Color.web(value).rgb()
            is Int -> rgbn(value)
            else -> throw IllegalArgumentException("Unable to coerce " + value.javaClass.name + " to Rgb")
        }

        @JvmStatic
        fun rgbn(n: Int) = Rgb((n shr 16 and 0xff).toDouble(), (n shr 8 and 0xff).toDouble(), (n and 0xff).toDouble(), 1.0)
    }

    override fun rgb(): Rgb = this

    override fun brighter(k: Double): Rgb {
        val t = if(k == BRIGHTER) BRIGHTER
                else pow(BRIGHTER, k)

        return Rgb(r * t,g * t, b * t, opacity)
    }

    override fun darker(k: Double): Rgb {
        val t = if(k == DARKER) DARKER
                else pow(DARKER, k)

        return Rgb(r * t,g * t,b * t, opacity)
    }

    override fun toColor(): Color = Color.rgb(r.clamp(0.0, 255.0).toInt(),
                                              g.clamp(0.0, 255.0).toInt(),
                                              b.clamp(0.0, 255.0).toInt(),
                                              opacity.clamp(0.0, 1.0))
}
