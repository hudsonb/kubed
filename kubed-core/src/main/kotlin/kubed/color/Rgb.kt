package kubed.color

import javafx.scene.paint.Color

/**
 * Returns a representation of this color in the Rgb color space
 */
fun Color.rgb(): Rgb = Rgb((red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt(),
        opacity)

class Rgb(var r: Int, var g: Int, var b: Int, var opacity: Double = 1.0) : ColorSpace<Rgb> {
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
        fun rgbn(n: Int) = Rgb(n shr 16 and 0xff, n shr 8 and 0xff, n and 0xff, 1.0)
    }

    override fun rgb(): Rgb = this

    override fun brighter(k: Double): Rgb {
        val t = if(k == BRIGHTER) BRIGHTER
                else Math.pow(BRIGHTER, k)

        return Rgb((r * t).toInt(),
                   (g * t).toInt(),
                   (b * t).toInt(),
                   opacity)
    }

    override fun darker(k: Double): Rgb {
        val t = if(k == DARKER) DARKER
                else Math.pow(DARKER, k)

        return Rgb((r * t).toInt(),
                   (g * t).toInt(),
                   (b * t).toInt(),
                   opacity)
    }

    override fun toColor(): Color = Color.rgb(r, g, b, opacity)
}
